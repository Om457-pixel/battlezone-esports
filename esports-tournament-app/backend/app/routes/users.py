from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from ..database import get_db
from datetime import datetime

users_bp = Blueprint('users', __name__)

@users_bp.route('/profile', methods=['GET'])
@jwt_required()
def get_profile():
    uid = get_jwt_identity()
    try:
        db = get_db()
        user = db.users.find_one({"uid": uid}, {"_id": 0})
        if not user:
            return jsonify({"error": "User not found"}), 404
        for field in ['created_at', 'updated_at', 'last_login', 'last_daily_reward']:
            if isinstance(user.get(field), datetime):
                user[field] = user[field].isoformat()
        return jsonify(user), 200
    except Exception:
        return jsonify({"error": "DB unavailable"}), 503

@users_bp.route('/profile', methods=['PUT'])
@jwt_required()
def update_profile():
    uid = get_jwt_identity()
    data = request.get_json()
    allowed = ['bio', 'avatar', 'notification_prefs']
    update = {k: v for k, v in data.items() if k in allowed}
    update['updated_at'] = datetime.utcnow()
    try:
        get_db().users.update_one({"uid": uid}, {"$set": update})
        return jsonify({"success": True}), 200
    except Exception:
        return jsonify({"error": "DB unavailable"}), 503

@users_bp.route('/<username>', methods=['GET'])
@jwt_required()
def get_user_profile(username):
    try:
        db = get_db()
        user = db.users.find_one({"username": username}, {
            "_id": 0, "uid": 0, "phone": 0, "email": 0,
            "wallet_balance": 0, "bonus_balance": 0
        })
        if not user:
            return jsonify({"error": "User not found"}), 404
        for field in ['created_at', 'last_login']:
            if isinstance(user.get(field), datetime):
                user[field] = user[field].isoformat()
        return jsonify(user), 200
    except Exception:
        return jsonify({"error": "DB unavailable"}), 503

@users_bp.route('/match-history', methods=['GET'])
@jwt_required()
def match_history():
    uid = get_jwt_identity()
    limit = int(request.args.get('limit', 10))
    skip = int(request.args.get('skip', 0))
    try:
        db = get_db()
        joined = list(db.joined_players.find({"user_id": uid}, {"_id": 0}).sort("joined_at", -1).skip(skip).limit(limit))
        match_ids = [j['match_id'] for j in joined]
        matches = {m['match_id']: m for m in db.matches.find({"match_id": {"$in": match_ids}}, {"_id": 0})}
        result = []
        for j in joined:
            m = matches.get(j['match_id'], {})
            result.append({
                "match_id": j['match_id'],
                "title": m.get('title'),
                "game": m.get('game'),
                "rank": j.get('rank'),
                "kills": j.get('kills'),
                "prize_won": j.get('prize_won'),
                "joined_at": j['joined_at'].isoformat() if isinstance(j.get('joined_at'), datetime) else None
            })
        return jsonify({"history": result}), 200
    except Exception:
        return jsonify({"history": [], "warning": "DB unavailable"}), 200

@users_bp.route('/report', methods=['POST'])
@jwt_required()
def report_user():
    uid = get_jwt_identity()
    data = request.get_json()
    try:
        db = get_db()
        db.reports.insert_one({
            "reporter_id": uid,
            "reported_user": data.get('reported_user'),
            "match_id": data.get('match_id'),
            "reason": data.get('reason'),
            "description": data.get('description'),
            "status": "pending",
            "created_at": datetime.utcnow()
        })
        return jsonify({"success": True, "message": "Report submitted for review"}), 200
    except Exception:
        return jsonify({"error": "DB unavailable"}), 503
