# BattleZone AI Analytics Engine 🎮⚡

An AI-powered esports analytics dashboard built on top of the BattleZone tournament platform.

## What it does
- **Live leaderboard analytics** — rank points, win rates, kill stats across all players
- **Player Scout** — deep dive into any player with win probability prediction (ML model)
- **AI Commentary** — Groq LLaMA3 generates hype esports commentary for match results
- **AI Player Insight** — AI analyses a player's strengths and weaknesses
- **Tournament Summary** — AI narrates overall tournament performance

## Stack
- Python + Streamlit (UI)
- Groq API — LLaMA3-8b (AI commentary & insights, free tier)
- MongoDB (BattleZone live data)
- Plotly (charts)
- Scikit-learn / NumPy (win probability model)

## Setup

```bash
cd ai-analytics
pip install -r requirements.txt
cp .env.example .env
# Fill in GROQ_API_KEY (free at console.groq.com) and MONGO_URI
streamlit run app.py
```

> Works in demo mode with sample data even without MongoDB connected.

## Get free Groq API key
1. Go to https://console.groq.com
2. Sign up → API Keys → Create key
3. Paste in `.env`
