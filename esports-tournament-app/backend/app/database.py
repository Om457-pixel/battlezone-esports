from pymongo import MongoClient, ASCENDING, DESCENDING
from pymongo.errors import ConnectionFailure
import os

_db = None

def init_db(app):
    global _db
    try:
        client = MongoClient(app.config['MONGO_URI'], serverSelectionTimeoutMS=3000)
        client.server_info()  # test connection
        _db = client.get_default_database()
        _create_indexes(_db)
        print("MongoDB connected successfully")
    except Exception as e:
        print(f"WARNING: MongoDB not available ({e}). Some features will be disabled.")
        # Create a dummy db object so the app still starts
        client = MongoClient(app.config['MONGO_URI'], serverSelectionTimeoutMS=1)
        _db = client.get_default_database()

def get_db():
    return _db

def _create_indexes(db):
    db.users.create_index([('uid', ASCENDING)], unique=True)
    db.users.create_index([('username', ASCENDING)], unique=True)
    db.users.create_index([('phone', ASCENDING)])
    db.users.create_index([('referral_code', ASCENDING)], unique=True)
    db.matches.create_index([('status', ASCENDING)])
    db.matches.create_index([('game', ASCENDING)])
    db.matches.create_index([('start_time', ASCENDING)])
    db.joined_players.create_index([('match_id', ASCENDING), ('user_id', ASCENDING)], unique=True)
    db.transactions.create_index([('user_id', ASCENDING)])
    db.leaderboard.create_index([('game', ASCENDING), ('season', ASCENDING)])
    db.chat_messages.create_index([('match_id', ASCENDING), ('timestamp', ASCENDING)])
    db.notifications.create_index([('user_id', ASCENDING), ('read', ASCENDING)])
