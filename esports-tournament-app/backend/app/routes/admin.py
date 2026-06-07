from flask import Blueprint, request, jsonify, current_app
from flask_jwt_extended import jwt_required, get_jwt_identity
from ..database import get_db
from ..models.match import create_match
from ..models.transaction import credit_wallet
from ..models.user import update_rank_tier
from ..routes.notifications import send_notification
from datetime import datetime
import functools

admin_bp = Blueprint('admin', __name__)

def admin_required(f):
    @functools.wraps(f)
    @jwt_required()
    def decorated(*args, **kwargs):
        uid = get_jwt_identity()
        db = get_db()
        user = db.users.find_one({"uid": uid})
        if not user or not user.get('is_admin', False):
            return jsonify({"error": "Admin access required"}), 403
        return f(*args, **kwargs)
    return decorated

@admin_bp.route('/create-match', methods=['POST'])
@admin_required
def admin_create_match():
    uid = get_jwt_identity()
    data = request.get_json()
    match = create_match(data, uid)
    return jsonify({"success": True, "match_id": match['match_id']}), 201

@admin_bp.route('/matches', methods=['GET'])
@admin_required
def admin_list_matches():
    db = get_db()
    matches = list(db.matches.find({}, {"_id": 0}).sort("created_at", -1).limit(50))
    for m in matches:
        for f in ['start_time', 'created_at']:
            if isinstance(m.get(f), datetime):
                m[f] = m[f].isoformat()
    return jsonify({"matches": matches}), 200

@admin_bp.route('/matches/<match_id>/set-room', methods=['POST'])
@admin_required
def set_room_credentials(match_id):
    data = request.get_json()
    db = get_db()
    db.matches.update_one({"match_id": match_id}, {"$set": {
        "room_id": data['room_id'],
        "room_password": data['room_password'],
        "room_revealed_at": datetime.utcnow()
    }})
    # Notify all joined players
    players = db.joined_players.find({"match_id": match_id}, {"user_id": 1})
    match = db.matches.find_one({"match_id": match_id})
    for p in players:
        send_notification(p['user_id'], "Room Details Available",
                         f"Room ID and password for {match['title']} are now available!",
                         "room_reveal", {"match_id": match_id})
    return jsonify({"success": True}), 200

@admin_bp.route('/matches/<match_id>/update-status', methods=['POST'])
@admin_required
def update_match_status(match_id):
    data = request.get_json()
    status = data.get('status')
    db = get_db()
    db.matches.update_one({"match_id": match_id}, {"$set": {"status": status, "updated_at": datetime.utcnow()}})
    return jsonify({"success": True}), 200

@admin_bp.route('/matches/<match_id>/distribute-prizes', methods=['POST'])
@admin_required
def distribute_prizes(match_id):
    db = get_db()
    match = db.matches.find_one({"match_id": match_id})
    if not match:
        return jsonify({"error": "Match not found"}), 404

    results = list(db.joined_players.find({"match_id": match_id}).sort("rank", 1))
    prize_dist = {p['rank']: p['prize'] for p in match.get('prize_distribution', [])}

    for player in results:
        rank = player.get('rank')
        if rank and rank in prize_dist:
            prize = prize_dist[rank]
            credit_wallet(player['user_id'], prize, "prize_won",
                         f"Prize for rank #{rank} in {match['title']}", match_id)
            db.joined_players.update_one(
                {"match_id": match_id, "user_id": player['user_id']},
                {"$set": {"prize_won": prize}}
            )
            send_notification(player['user_id'], "Prize Credited!",
                            f"You won ₹{prize} for rank #{rank} in {match['title']}",
                            "prize", {"match_id": match_id, "prize": prize})
            # Update stats
            db.users.update_one({"uid": player['user_id']}, {
                "$inc": {
                    "stats.matches_played": 1,
                    "stats.total_kills": player.get('kills', 0),
                    "stats.matches_won": 1 if rank == 1 else 0,
                    "stats.rank_points": max(0, (10 - rank) * 50 + player.get('kills', 0) * 10)
                }
            })
        else:
            db.users.update_one({"uid": player['user_id']}, {
                "$inc": {"stats.matches_played": 1, "stats.total_kills": player.get('kills', 0)}
            })

    db.matches.update_one({"match_id": match_id}, {"$set": {"status": "completed"}})
    return jsonify({"success": True, "distributed_to": len(results)}), 200

@admin_bp.route('/reports', methods=['GET'])
@admin_required
def get_reports():
    db = get_db()
    reports = list(db.reports.find({"status": "pending"}, {"_id": 0}).limit(50))
    return jsonify({"reports": reports}), 200

@admin_bp.route('/users', methods=['GET'])
@admin_required
def list_users():
    db = get_db()
    users = list(db.users.find({}, {"_id": 0, "uid": 1, "username": 1, "level": 1,
                                     "wallet_balance": 1, "is_banned": 1, "trust_score": 1}).limit(100))
    return jsonify({"users": users}), 200

@admin_bp.route('/users/<uid>/ban', methods=['POST'])
@admin_required
def ban_user(uid):
    data = request.get_json()
    get_db().users.update_one({"uid": uid}, {"$set": {"is_banned": data.get('banned', True)}})
    return jsonify({"success": True}), 200
