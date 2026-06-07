import pandas as pd
import numpy as np
from db import get_db, get_sample_data


def load_data():
    """Load player and match data from MongoDB or fall back to sample data."""
    try:
        db = get_db()
        # Players
        users = list(db.users.find({}, {
            "_id": 0, "username": 1, "stats": 1, "rank_tier": 1,
            "level": 1, "total_earnings": 1
        }))
        # Match results
        results = list(db.joined_players.find({}, {
            "_id": 0, "match_id": 1, "username": 1, "rank": 1,
            "kills": 1, "prize_won": 1
        }))
        # Enrich results with game info
        match_ids = list({r["match_id"] for r in results})
        matches = {m["match_id"]: m["game"] for m in db.matches.find(
            {"match_id": {"$in": match_ids}}, {"_id": 0, "match_id": 1, "game": 1}
        )}
        for r in results:
            r["game"] = matches.get(r["match_id"], "Unknown")

        if not users:
            raise ValueError("Empty DB")

        players_df = pd.DataFrame([{
            "username": u.get("username", "?"),
            "game": u.get("preferred_game", "Unknown"),
            "rank_tier": u.get("rank_tier", "Bronze"),
            "matches_played": u.get("stats", {}).get("matches_played", 0),
            "matches_won": u.get("stats", {}).get("matches_won", 0),
            "total_kills": u.get("stats", {}).get("total_kills", 0),
            "win_rate": u.get("stats", {}).get("win_rate", 0.0),
            "rank_points": u.get("stats", {}).get("rank_points", 0),
            "total_earnings": u.get("total_earnings", 0.0),
            "level": u.get("level", 1),
        } for u in users])

        results_df = pd.DataFrame(results) if results else pd.DataFrame(
            columns=["match_id", "username", "rank", "kills", "prize_won", "game"]
        )
        return players_df, results_df, False  # False = live data

    except Exception:
        players_df, results_df = get_sample_data()
        return players_df, results_df, True  # True = demo data


def top_players(players_df, n=10, sort_by="rank_points"):
    return players_df.nlargest(n, sort_by).reset_index(drop=True)


def game_stats(results_df):
    if results_df.empty:
        return pd.DataFrame()
    return results_df.groupby("game").agg(
        total_matches=("match_id", "nunique"),
        avg_kills=("kills", "mean"),
        total_prize_distributed=("prize_won", "sum"),
    ).round(2).reset_index()


def player_performance(results_df, username):
    df = results_df[results_df["username"] == username].copy()
    if df.empty:
        return None
    return {
        "matches": len(df),
        "avg_kills": round(df["kills"].mean(), 2),
        "avg_rank": round(df["rank"].mean(), 2),
        "best_rank": int(df["rank"].min()),
        "total_prize": round(df["prize_won"].sum(), 2),
        "win_count": int((df["rank"] == 1).sum()),
    }


def predict_win_probability(players_df, username):
    """Simple logistic-regression-style score based on player stats."""
    player = players_df[players_df["username"] == username]
    if player.empty:
        return None

    p = player.iloc[0]
    # Normalise stats against top player
    max_rp = players_df["rank_points"].max() or 1
    max_wr = players_df["win_rate"].max() or 1
    max_kd = (players_df["total_kills"] / players_df["matches_played"].replace(0, 1)).max() or 1

    kd = p["total_kills"] / max(p["matches_played"], 1)
    score = (
        0.40 * (p["rank_points"] / max_rp) +
        0.35 * (p["win_rate"] / max_wr) +
        0.25 * (kd / max_kd)
    )
    return round(min(score * 100, 99.9), 1)


def tier_distribution(players_df):
    order = ["Bronze", "Silver", "Gold", "Platinum", "Diamond", "Master", "Legend"]
    counts = players_df["rank_tier"].value_counts().reindex(order, fill_value=0).reset_index()
    counts.columns = ["tier", "count"]
    return counts
