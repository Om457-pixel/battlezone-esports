import os
from groq import Groq
from dotenv import load_dotenv

load_dotenv()

_client = None

def _get_client():
    global _client
    if _client is None:
        api_key = os.getenv("GROQ_API_KEY")
        if not api_key:
            raise ValueError("GROQ_API_KEY not set in .env")
        _client = Groq(api_key=api_key)
    return _client


def generate_match_commentary(match_data: dict) -> str:
    """
    Generate hype esports commentary for a match result.
    match_data: { game, winner, winner_kills, top_players: [{username, rank, kills}], prize_pool }
    """
    top = match_data.get("top_players", [])
    top_str = "\n".join(
        f"  #{p['rank']} {p['username']} — {p['kills']} kills" for p in top[:5]
    )
    prompt = f"""You are a hype esports commentator for a mobile gaming tournament.
Write exciting, short commentary (3-4 sentences) for this match result.
Be energetic, use gaming slang, mention specific players and stats.

Game: {match_data.get('game', 'Unknown')}
Winner: {match_data.get('winner', 'Unknown')} with {match_data.get('winner_kills', 0)} kills
Prize Pool: ₹{match_data.get('prize_pool', 0)}
Top Players:
{top_str}

Write only the commentary, no intro text."""

    try:
        response = _get_client().chat.completions.create(
            model="llama3-8b-8192",
            messages=[{"role": "user", "content": prompt}],
            max_tokens=200,
            temperature=0.85,
        )
        return response.choices[0].message.content.strip()
    except Exception as e:
        return f"⚡ What a match! {match_data.get('winner', 'The champion')} dominated with {match_data.get('winner_kills', 0)} kills to claim the top spot!"


def generate_player_insight(stats: dict, username: str) -> str:
    """Generate AI insight for a player's performance."""
    prompt = f"""You are an esports performance analyst. Give a short, sharp analysis (2-3 sentences) 
of this player's stats. Be direct, mention strengths and what they should improve.

Player: {username}
Matches Played: {stats.get('matches', 0)}
Average Kills: {stats.get('avg_kills', 0)}
Average Rank: {stats.get('avg_rank', 0)}
Best Rank: {stats.get('best_rank', 'N/A')}
Win Count: {stats.get('win_count', 0)}
Total Prize Won: ₹{stats.get('total_prize', 0)}

Write only the analysis."""

    try:
        response = _get_client().chat.completions.create(
            model="llama3-8b-8192",
            messages=[{"role": "user", "content": prompt}],
            max_tokens=150,
            temperature=0.7,
        )
        return response.choices[0].message.content.strip()
    except Exception as e:
        return f"📊 {username} has shown consistent performance. Keep grinding to climb the ranks!"


def generate_tournament_summary(game_stats: dict) -> str:
    """Generate an overall tournament summary."""
    prompt = f"""You are an esports analyst. Write a punchy 3-sentence tournament summary.

Stats:
{game_stats}

Focus on the most interesting numbers. Write only the summary."""

    try:
        response = _get_client().chat.completions.create(
            model="llama3-8b-8192",
            messages=[{"role": "user", "content": prompt}],
            max_tokens=180,
            temperature=0.75,
        )
        return response.choices[0].message.content.strip()
    except Exception as e:
        return "🏆 The tournament has been action-packed with incredible performances across all games!"
