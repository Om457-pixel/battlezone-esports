from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from ..database import get_db

leaderboard_bp = Blueprint('leaderboard', __name__)

@leaderboard_bp.route('/', methods=['GET'])
@jwt_required()
def get_leaderboard():
    game = request.args.get('game', 'all')
    period = request.args.get('period', 'weekly')
    limit = int(request.args.get('limit', 50))
    try:
        db = get_db()
        query = {} if game == 'all' else {"preferred_game": game}
        users = list(db.users.find(query, {
            "_id": 0, "uid": 1, "username": 1, "avatar": 1,
            "level": 1, "rank_tier": 1, "stats": 1, "total_earnings": 1
        }).sort("stats.rank_points", -1).limit(limit))
        for i, u in enumerate(users):
            u['rank'] = i + 1
        return jsonify({"leaderboard": users, "period": period, "game": game}), 200
    except Exception as e:
        return jsonify({"leaderboard": [], "period": period, "game": game, "warning": "DB unavailable"}), 200

@leaderboard_bp.route('/my-rank', methods=['GET'])
@jwt_required()
def my_rank():
    uid = get_jwt_identity()
    try:
        db = get_db()
        user = db.users.find_one({"uid": uid}, {"stats.rank_points": 1})
        if not user:
            return jsonify({"error": "User not found"}), 404
        rank_points = user['stats']['rank_points']
        rank = db.users.count_documents({"stats.rank_points": {"$gt": rank_points}}) + 1
        return jsonify({"rank": rank, "rank_points": rank_points}), 200
    except Exception:
        return jsonify({"rank": 0, "rank_points": 0, "warning": "DB unavailable"}), 200
