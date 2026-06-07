from datetime import datetime
from ..database import get_db
import uuid

MATCH_STATUS = ["upcoming", "live", "completed", "cancelled"]
GAMES = ["Free Fire MAX", "PUBG Mobile", "Call of Duty Mobile", "Battlegrounds Mobile India"]

def create_match(data, created_by):
    db = get_db()
    match = {
        "match_id": str(uuid.uuid4()),
        "title": data['title'],
        "game": data['game'],
        "mode": data.get('mode', 'Squad'),
        "map": data.get('map', 'Bermuda'),
        "entry_fee": float(data['entry_fee']),
        "prize_pool": float(data['prize_pool']),
        "prize_distribution": data.get('prize_distribution', [
            {"rank": 1, "prize": float(data['prize_pool']) * 0.5},
            {"rank": 2, "prize": float(data['prize_pool']) * 0.3},
            {"rank": 3, "prize": float(data['prize_pool']) * 0.2}
        ]),
        "max_players": int(data['max_players']),
        "current_players": 0,
        "start_time": datetime.fromisoformat(data['start_time']),
        "status": "upcoming",
        "room_id": None,
        "room_password": None,
        "room_revealed_at": None,
        "banner_url": data.get('banner_url'),
        "rules": data.get('rules', ''),
        "created_by": created_by,
        "is_featured": data.get('is_featured', False),
        "tournament_bracket": data.get('is_tournament', False),
        "bracket_data": None,
        "created_at": datetime.utcnow(),
        "updated_at": datetime.utcnow()
    }
    db.matches.insert_one(match)
    return match

def get_matches_by_status(status, game=None, limit=20, skip=0):
    db = get_db()
    query = {"status": status}
    if game:
        query["game"] = game
    return list(db.matches.find(query, {"_id": 0}).sort("start_time", 1).skip(skip).limit(limit))

def join_match(match_id, user_id, in_game_name, team_name=None):
    db = get_db()
    match = db.matches.find_one({"match_id": match_id})
    if not match:
        return {"error": "Match not found"}
    if match['status'] != 'upcoming':
        return {"error": "Match is not open for joining"}
    if match['current_players'] >= match['max_players']:
        return {"error": "Match is full"}
    existing = db.joined_players.find_one({"match_id": match_id, "user_id": user_id})
    if existing:
        return {"error": "Already joined this match"}
    slot = match['current_players'] + 1
    db.joined_players.insert_one({
        "match_id": match_id,
        "user_id": user_id,
        "in_game_name": in_game_name,
        "team_name": team_name,
        "slot": slot,
        "kills": 0,
        "rank": None,
        "prize_won": 0.0,
        "result_screenshot": None,
        "joined_at": datetime.utcnow()
    })
    db.matches.update_one({"match_id": match_id}, {
        "$inc": {"current_players": 1},
        "$set": {"updated_at": datetime.utcnow()}
    })
    return {"success": True, "slot": slot}

def submit_result(match_id, user_id, rank, kills, screenshot_url):
    db = get_db()
    db.joined_players.update_one(
        {"match_id": match_id, "user_id": user_id},
        {"$set": {"rank": rank, "kills": kills, "result_screenshot": screenshot_url}}
    )
    return {"success": True}
