from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from ..database import get_db
from ..models.match import create_match, get_matches_by_status, join_match, submit_result
from ..models.transaction import debit_wallet, credit_wallet
from ..models.user import update_user_xp
from datetime import datetime, timedelta

matches_bp = Blueprint('matches', __name__)

@matches_bp.route('/', methods=['GET'])
@jwt_required()
def list_matches():
    status = request.args.get('status', 'upcoming')
    game = request.args.get('game')
    limit = int(request.args.get('limit', 20))
    skip = int(request.args.get('skip', 0))
    matches = get_matches_by_status(status, game, limit, skip)
    # Convert datetime to ISO string
    for m in matches:
        if 'start_time' in m and isinstance(m['start_time'], datetime):
            m['start_time'] = m['start_time'].isoformat()
    return jsonify({"matches": matches, "count": len(matches)}), 200

@matches_bp.route('/<match_id>', methods=['GET'])
@jwt_required()
def get_match(match_id):
    db = get_db()
    match = db.matches.find_one({"match_id": match_id}, {"_id": 0})
    if not match:
        return jsonify({"error": "Match not found"}), 404
    if isinstance(match.get('start_time'), datetime):
        match['start_time'] = match['start_time'].isoformat()
    # Reveal room ID 15 min before start
    now = datetime.utcnow()
    start = match.get('start_time') if isinstance(match.get('start_time'), datetime) else datetime.fromisoformat(match['start_time'])
    if (start - now).total_seconds() > 900:
        match['room_id'] = None
        match['room_password'] = None
    return jsonify(match), 200

@matches_bp.route('/<match_id>/join', methods=['POST'])
@jwt_required()
def join(match_id):
    uid = get_jwt_identity()
    data = request.get_json()
    in_game_name = data.get('in_game_name')
    if not in_game_name:
        return jsonify({"error": "In-game name required"}), 400

    db = get_db()
    match = db.matches.find_one({"match_id": match_id})
    if not match:
        return jsonify({"error": "Match not found"}), 404

    entry_fee = match['entry_fee']
    if entry_fee > 0:
        result = debit_wallet(uid, entry_fee, "entry_fee", f"Entry fee for {match['title']}", match_id)
        if "error" in result:
            return jsonify(result), 400

    result = join_match(match_id, uid, in_game_name, data.get('team_name'))
    if "error" in result:
        if entry_fee > 0:
            credit_wallet(uid, entry_fee, "refund", f"Refund for {match['title']}", match_id)
        return jsonify(result), 400

    return jsonify(result), 200

@matches_bp.route('/<match_id>/result', methods=['POST'])
@jwt_required()
def submit_match_result(match_id):
    uid = get_jwt_identity()
    data = request.get_json()
    rank = data.get('rank')
    kills = data.get('kills', 0)
    screenshot_url = data.get('screenshot_url')

    if rank is None:
        return jsonify({"error": "Rank required"}), 400

    result = submit_result(match_id, uid, rank, kills, screenshot_url)
    return jsonify(result), 200

@matches_bp.route('/<match_id>/players', methods=['GET'])
@jwt_required()
def get_players(match_id):
    db = get_db()
    players = list(db.joined_players.find({"match_id": match_id}, {"_id": 0}))
    return jsonify({"players": players}), 200

@matches_bp.route('/featured', methods=['GET'])
@jwt_required()
def featured_matches():
    db = get_db()
    matches = list(db.matches.find(
        {"is_featured": True, "status": {"$in": ["upcoming", "live"]}},
        {"_id": 0}
    ).limit(5))
    for m in matches:
        if isinstance(m.get('start_time'), datetime):
            m['start_time'] = m['start_time'].isoformat()
    return jsonify({"matches": matches}), 200
