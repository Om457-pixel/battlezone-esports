from pymongo import MongoClient
from dotenv import load_dotenv
import os

load_dotenv()

_client = None

def get_db():
    global _client
    if _client is None:
        uri = os.getenv("MONGO_URI", "mongodb://localhost:27017/esports_db")
        _client = MongoClient(uri)
    db_name = os.getenv("MONGO_URI", "mongodb://localhost:27017/esports_db").split("/")[-1]
    return _client[db_name]


def get_sample_data():
    """Returns mock data when MongoDB is not connected, for demo purposes."""
    import pandas as pd
    import numpy as np
    import random

    random.seed(42)
    np.random.seed(42)

    games = ["Free Fire MAX", "PUBG Mobile", "Call of Duty Mobile", "BGMI"]
    tiers = ["Bronze", "Silver", "Gold", "Platinum", "Diamond", "Master", "Legend"]
    usernames = [
        "SnipeKing", "BlazeShot", "ProGamer99", "DarkWolf", "NightHunter",
        "ShadowStrike", "EliteForce", "ThunderBolt", "IronFist", "CyberNinja",
        "PixelWarrior", "GhostRider", "FireStorm", "DeathBringer", "LegendSlayer"
    ]

    players = []
    for i, name in enumerate(usernames):
        matches_played = random.randint(10, 80)
        wins = random.randint(1, matches_played // 2)
        players.append({
            "username": name,
            "game": random.choice(games),
            "rank_tier": tiers[min(i // 2, len(tiers) - 1)],
            "matches_played": matches_played,
            "matches_won": wins,
            "total_kills": random.randint(matches_played * 2, matches_played * 8),
            "win_rate": round(wins / matches_played * 100, 1),
            "rank_points": random.randint(i * 700, (i + 1) * 1000),
            "total_earnings": round(random.uniform(0, 5000), 2),
            "level": random.randint(1, 30),
        })

    match_results = []
    for match_num in range(1, 21):
        game = random.choice(games)
        participants = random.sample(usernames, min(10, len(usernames)))
        for rank, player in enumerate(participants, 1):
            match_results.append({
                "match_id": f"match_{match_num:03d}",
                "game": game,
                "username": player,
                "rank": rank,
                "kills": random.randint(0, 15),
                "prize_won": round(500 / rank, 2) if rank <= 3 else 0.0,
            })

    return pd.DataFrame(players), pd.DataFrame(match_results)
