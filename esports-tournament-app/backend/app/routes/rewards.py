from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from ..database import get_db
from ..models.transaction import credit_wallet
from datetime import datetime, date
import random

rewards_bp = Blueprint('rewards', __name__)

DAILY_REWARDS = [10, 15, 20, 25, 30, 40, 75]  # 7-day streak rewards
SPIN_PRIZES = [
    {"label": "₹5", "amount": 5, "probability": 0.30},
    {"label": "₹10", "amount": 10, "probability": 0.25},
    {"label": "₹25", "amount": 25, "probability": 0.20},
    {"label": "₹50", "amount": 50, "probability": 0.12},
    {"label": "₹100", "amount": 100, "probability": 0.08},
    {"label": "₹250", "amount": 250, "probability": 0.04},
    {"label": "₹500", "amount": 500, "probability": 0.01},
]

ACHIEVEMENTS = [
    {"id": "first_match", "title": "First Blood", "desc": "Play your first match", "xp": 50, "icon": "🎮"},
    {"id": "first_win", "title": "Winner Winner", "desc": "Win your first match", "xp": 100, "icon": "🏆"},
    {"id": "kill_streak_10", "title": "Killing Machine", "desc": "Get 10 kills in a match", "xp": 150, "icon": "💀"},
    {"id": "streak_7", "title": "Week Warrior", "desc": "7-day login streak", "xp": 200, "icon": "🔥"},
    {"id": "matches_10", "title": "Veteran", "desc": "Play 10 matches", "xp": 300, "icon": "⚔️"},
    {"id": "earnings_1000", "title": "Money Maker", "desc": "Earn ₹1000 total", "xp": 500, "icon": "💰"},
]

@rewards_bp.route('/daily-claim', methods=['POST'])
@jwt_required()
def claim_daily_reward():
    uid = get_jwt_identity()
    db = get_db()
    user = db.users.find_one({"uid": uid})
    if not user:
        return jsonify({"error": "User not found"}), 404

    today = date.today()
    last_claim = user.get('last_daily_reward')
    if last_claim and (isinstance(last_claim, datetime) and last_claim.date() == today):
        return jsonify({"error": "Already claimed today"}), 400

    streak = user.get('streak', 1)
    day_index = min(streak - 1, 6)
    reward_amount = DAILY_REWARDS[day_index]

    credit_wallet(uid, reward_amount, "daily_reward", f"Day {streak} login reward")
    db.users.update_one({"uid": uid}, {"$set": {"last_daily_reward": datetime.utcnow()}})

    return jsonify({
        "success": True,
        "amount": reward_amount,
        "streak": streak,
        "next_reward": DAILY_REWARDS[min(day_index + 1, 6)]
    }), 200

@rewards_bp.route('/spin', methods=['POST'])
@jwt_required()
def spin_wheel():
    uid = get_jwt_identity()
    db = get_db()
    user = db.users.find_one({"uid": uid})
    if not user:
        return jsonify({"error": "User not found"}), 404
    if not user.get('spin_available', False):
        return jsonify({"error": "No spins available"}), 400

    # Weighted random selection
    rand = random.random()
    cumulative = 0
    prize = SPIN_PRIZES[-1]
    for p in SPIN_PRIZES:
        cumulative += p['probability']
        if rand <= cumulative:
            prize = p
            break

    credit_wallet(uid, prize['amount'], "spin_reward", f"Spin wheel reward: {prize['label']}")
    db.users.update_one({"uid": uid}, {"$set": {"spin_available": False}})

    return jsonify({"success": True, "prize": prize}), 200

@rewards_bp.route('/achievements', methods=['GET'])
@jwt_required()
def get_achievements():
    uid = get_jwt_identity()
    db = get_db()
    user = db.users.find_one({"uid": uid}, {"achievements": 1})
    earned_ids = [a['id'] for a in user.get('achievements', [])]
    result = []
    for ach in ACHIEVEMENTS:
        result.append({**ach, "earned": ach['id'] in earned_ids})
    return jsonify({"achievements": result}), 200

@rewards_bp.route('/daily-schedule', methods=['GET'])
@jwt_required()
def daily_schedule():
    uid = get_jwt_identity()
    db = get_db()
    user = db.users.find_one({"uid": uid}, {"streak": 1, "last_daily_reward": 1})
    streak = user.get('streak', 0)
    last_claim = user.get('last_daily_reward')
    today = date.today()
    claimed_today = last_claim and isinstance(last_claim, datetime) and last_claim.date() == today
    return jsonify({
        "rewards": DAILY_REWARDS,
        "current_streak": streak,
        "claimed_today": claimed_today,
        "current_day": min(streak, 7)
    }), 200
