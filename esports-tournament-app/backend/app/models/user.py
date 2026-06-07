from datetime import datetime
import random
import string
from ..database import get_db

def generate_referral_code():
    return ''.join(random.choices(string.ascii_uppercase + string.digits, k=8))

def create_user(uid, username, phone=None, email=None, referred_by=None):
    db = get_db()
    user = {
        "uid": uid,
        "username": username,
        "phone": phone,
        "email": email,
        "avatar": None,
        "bio": "",
        "level": 1,
        "xp": 0,
        "xp_to_next_level": 100,
        "wallet_balance": 0.0,
        "bonus_balance": 0.0,
        "total_earnings": 0.0,
        "referral_code": generate_referral_code(),
        "referred_by": referred_by,
        "stats": {
            "matches_played": 0,
            "matches_won": 0,
            "total_kills": 0,
            "win_rate": 0.0,
            "rank_points": 0
        },
        "rank_tier": "Bronze",
        "trust_score": 100,
        "achievements": [],
        "badges": [],
        "streak": 0,
        "last_login": datetime.utcnow(),
        "last_daily_reward": None,
        "spin_available": True,
        "is_banned": False,
        "is_verified": False,
        "notification_prefs": {
            "match_reminders": True,
            "results": True,
            "rewards": True,
            "promotions": True
        },
        "created_at": datetime.utcnow(),
        "updated_at": datetime.utcnow()
    }
    db.users.insert_one(user)
    return user

def get_user_by_uid(uid):
    return get_db().users.find_one({"uid": uid}, {"_id": 0})

def get_user_by_username(username):
    return get_db().users.find_one({"username": username}, {"_id": 0})

def update_user_xp(uid, xp_gained):
    db = get_db()
    user = db.users.find_one({"uid": uid})
    if not user:
        return None
    new_xp = user['xp'] + xp_gained
    new_level = user['level']
    xp_to_next = user['xp_to_next_level']
    while new_xp >= xp_to_next:
        new_xp -= xp_to_next
        new_level += 1
        xp_to_next = int(xp_to_next * 1.5)
    db.users.update_one({"uid": uid}, {"$set": {
        "xp": new_xp, "level": new_level,
        "xp_to_next_level": xp_to_next,
        "updated_at": datetime.utcnow()
    }})
    return {"level": new_level, "xp": new_xp, "leveled_up": new_level > user['level']}

def update_rank_tier(uid, rank_points):
    tiers = [
        (0, "Bronze"), (500, "Silver"), (1500, "Gold"),
        (3000, "Platinum"), (6000, "Diamond"), (10000, "Master"), (15000, "Legend")
    ]
    tier = "Bronze"
    for threshold, name in tiers:
        if rank_points >= threshold:
            tier = name
    get_db().users.update_one({"uid": uid}, {"$set": {"rank_tier": tier, "stats.rank_points": rank_points}})
    return tier
