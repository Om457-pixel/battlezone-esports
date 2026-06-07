import streamlit as st
import plotly.express as px
import plotly.graph_objects as go
import pandas as pd
from analytics import load_data, top_players, game_stats, player_performance, predict_win_probability, tier_distribution
from ai_commentary import generate_match_commentary, generate_player_insight, generate_tournament_summary

# ── Page config ──────────────────────────────────────────────────────────────
st.set_page_config(
    page_title="BattleZone AI Analytics",
    page_icon="🎮",
    layout="wide",
    initial_sidebar_state="expanded",
)

# ── Custom CSS ────────────────────────────────────────────────────────────────
st.markdown("""
<style>
    .main { background-color: #0e0e1a; }
    .stApp { background: linear-gradient(135deg, #0e0e1a 0%, #1a0a2e 100%); }
    .metric-card {
        background: linear-gradient(135deg, #1e1e3a, #2a1a4e);
        border: 1px solid #6c3fc5;
        border-radius: 12px;
        padding: 20px;
        text-align: center;
    }
    .neon-title {
        color: #a855f7;
        text-shadow: 0 0 20px #a855f7;
        font-size: 2.5rem;
        font-weight: 800;
    }
    .commentary-box {
        background: linear-gradient(135deg, #1a0a2e, #0e1a2e);
        border-left: 4px solid #a855f7;
        border-radius: 8px;
        padding: 16px;
        font-style: italic;
        color: #e2d9f3;
    }
    div[data-testid="metric-container"] {
        background: linear-gradient(135deg, #1e1e3a, #2a1a4e);
        border: 1px solid #6c3fc5;
        border-radius: 10px;
        padding: 10px;
    }
</style>
""", unsafe_allow_html=True)

# ── Load data ─────────────────────────────────────────────────────────────────
@st.cache_data(ttl=60)
def load():
    return load_data()

players_df, results_df, is_demo = load()

# ── Sidebar ───────────────────────────────────────────────────────────────────
with st.sidebar:
    st.markdown("## 🎮 BattleZone")
    st.markdown("### AI Analytics Engine")
    st.divider()

    if is_demo:
        st.warning("⚡ Demo mode — sample data. Connect MongoDB for live data.")
    else:
        st.success("✅ Live data connected")

    st.divider()
    page = st.radio("Navigate", [
        "🏠 Overview",
        "🏆 Leaderboard",
        "🔍 Player Scout",
        "🎙️ AI Commentary",
    ])
    st.divider()
    st.caption("Powered by Groq LLaMA3 + BattleZone MongoDB")

# ── OVERVIEW ──────────────────────────────────────────────────────────────────
if page == "🏠 Overview":
    st.markdown('<p class="neon-title">⚡ BattleZone Analytics</p>', unsafe_allow_html=True)
    st.caption("Real-time esports intelligence powered by AI")
    st.divider()

    # KPI row
    col1, col2, col3, col4 = st.columns(4)
    col1.metric("👥 Total Players", len(players_df))
    col2.metric("🎮 Matches Played", results_df["match_id"].nunique() if not results_df.empty else 0)
    col3.metric("💰 Prize Distributed", f"₹{results_df['prize_won'].sum():,.0f}" if not results_df.empty else "₹0")
    col4.metric("⚔️ Total Kills", f"{players_df['total_kills'].sum():,}")

    st.divider()
    col_left, col_right = st.columns(2)

    with col_left:
        st.subheader("🏅 Rank Tier Distribution")
        tier_df = tier_distribution(players_df)
        colors = ["#cd7f32","#c0c0c0","#ffd700","#00c8ff","#b9f2ff","#ff6ef7","#ff4444"]
        fig = px.bar(tier_df, x="tier", y="count", color="tier",
                     color_discrete_sequence=colors,
                     template="plotly_dark")
        fig.update_layout(showlegend=False, plot_bgcolor="rgba(0,0,0,0)",
                          paper_bgcolor="rgba(0,0,0,0)", margin=dict(t=10))
        st.plotly_chart(fig, use_container_width=True)

    with col_right:
        st.subheader("🎮 Kills by Game")
        if not results_df.empty:
            kills_by_game = results_df.groupby("game")["kills"].sum().reset_index()
            fig2 = px.pie(kills_by_game, names="game", values="kills",
                          color_discrete_sequence=px.colors.sequential.Purples_r,
                          template="plotly_dark", hole=0.4)
            fig2.update_layout(paper_bgcolor="rgba(0,0,0,0)", margin=dict(t=10))
            st.plotly_chart(fig2, use_container_width=True)
        else:
            st.info("No match result data yet.")

    # Game stats table
    st.subheader("📊 Game Performance Stats")
    gstats = game_stats(results_df)
    if not gstats.empty:
        st.dataframe(gstats, use_container_width=True)

    # AI Tournament Summary
    st.subheader("🤖 AI Tournament Summary")
    if st.button("Generate AI Summary", type="primary"):
        with st.spinner("AI is analysing the tournament..."):
            summary_data = gstats.to_string() if not gstats.empty else "No data"
            summary = generate_tournament_summary(summary_data)
            st.markdown(f'<div class="commentary-box">💬 {summary}</div>', unsafe_allow_html=True)

# ── LEADERBOARD ───────────────────────────────────────────────────────────────
elif page == "🏆 Leaderboard":
    st.markdown('<p class="neon-title">🏆 Leaderboard</p>', unsafe_allow_html=True)
    st.divider()

    col1, col2 = st.columns([2, 1])
    with col1:
        sort_by = st.selectbox("Sort by", ["rank_points", "total_kills", "win_rate", "total_earnings", "matches_played"])
    with col2:
        top_n = st.slider("Show top", 5, 15, 10)

    top_df = top_players(players_df, n=top_n, sort_by=sort_by)

    # Medal emojis for top 3
    def medal(i):
        return ["🥇", "🥈", "🥉"][i] if i < 3 else f"#{i+1}"

    top_df.insert(0, "Rank", [medal(i) for i in range(len(top_df))])

    st.dataframe(
        top_df[["Rank", "username", "rank_tier", "rank_points", "win_rate", "total_kills", "total_earnings", "level"]],
        use_container_width=True,
        hide_index=True,
    )

    st.divider()
    st.subheader("📈 Rank Points — Top Players")
    fig = px.bar(top_df, x="username", y="rank_points", color="rank_tier",
                 template="plotly_dark",
                 color_discrete_map={
                     "Bronze":"#cd7f32","Silver":"#c0c0c0","Gold":"#ffd700",
                     "Platinum":"#00c8ff","Diamond":"#b9f2ff","Master":"#ff6ef7","Legend":"#ff4444"
                 })
    fig.update_layout(paper_bgcolor="rgba(0,0,0,0)", plot_bgcolor="rgba(0,0,0,0)")
    st.plotly_chart(fig, use_container_width=True)

# ── PLAYER SCOUT ──────────────────────────────────────────────────────────────
elif page == "🔍 Player Scout":
    st.markdown('<p class="neon-title">🔍 Player Scout</p>', unsafe_allow_html=True)
    st.caption("Deep dive into any player's stats + AI insight")
    st.divider()

    usernames = sorted(players_df["username"].tolist())
    selected = st.selectbox("Select a player", usernames)

    if selected:
        player_row = players_df[players_df["username"] == selected].iloc[0]
        perf = player_performance(results_df, selected)
        win_prob = predict_win_probability(players_df, selected)

        # Stats row
        col1, col2, col3, col4, col5 = st.columns(5)
        col1.metric("🎖️ Tier", player_row["rank_tier"])
        col2.metric("⭐ Level", int(player_row["level"]))
        col3.metric("🏆 Rank Points", int(player_row["rank_points"]))
        col4.metric("📊 Win Rate", f"{player_row['win_rate']}%")
        col5.metric("🎯 Win Probability", f"{win_prob}%" if win_prob else "N/A")

        st.divider()

        col_l, col_r = st.columns(2)

        with col_l:
            if perf:
                st.subheader("📋 Match History Stats")
                st.metric("Matches Played", perf["matches"])
                st.metric("Avg Kills / Match", perf["avg_kills"])
                st.metric("Avg Rank", perf["avg_rank"])
                st.metric("Best Rank Achieved", f"#{perf['best_rank']}")
                st.metric("Total Prize Won", f"₹{perf['total_prize']:,.2f}")
            else:
                st.info("No match history found for this player.")

        with col_r:
            # Win probability gauge
            st.subheader("🎯 Win Probability Gauge")
            fig = go.Figure(go.Indicator(
                mode="gauge+number",
                value=win_prob or 0,
                number={"suffix": "%", "font": {"color": "#a855f7"}},
                gauge={
                    "axis": {"range": [0, 100], "tickcolor": "#a855f7"},
                    "bar": {"color": "#a855f7"},
                    "bgcolor": "#1e1e3a",
                    "steps": [
                        {"range": [0, 33], "color": "#2d1b4e"},
                        {"range": [33, 66], "color": "#3d2060"},
                        {"range": [66, 100], "color": "#5a2d82"},
                    ],
                    "threshold": {"line": {"color": "#ff6ef7", "width": 3}, "value": win_prob or 0}
                }
            ))
            fig.update_layout(
                paper_bgcolor="rgba(0,0,0,0)",
                font={"color": "#e2d9f3"},
                height=280,
                margin=dict(t=20, b=20)
            )
            st.plotly_chart(fig, use_container_width=True)

        # AI Insight
        st.divider()
        st.subheader("🤖 AI Player Insight")
        if st.button("Generate AI Insight", type="primary"):
            if perf:
                with st.spinner(f"Analysing {selected}..."):
                    insight = generate_player_insight(perf, selected)
                    st.markdown(f'<div class="commentary-box">💬 {insight}</div>', unsafe_allow_html=True)
            else:
                st.warning("Need match history to generate insight.")

        # Kill distribution chart
        if perf and not results_df.empty:
            st.divider()
            st.subheader("⚔️ Kill Distribution Across Matches")
            player_matches = results_df[results_df["username"] == selected]
            if not player_matches.empty:
                fig2 = px.bar(player_matches, x="match_id", y="kills", color="kills",
                              color_continuous_scale="Purples", template="plotly_dark")
                fig2.update_layout(paper_bgcolor="rgba(0,0,0,0)", plot_bgcolor="rgba(0,0,0,0)",
                                   xaxis_title="Match", yaxis_title="Kills")
                st.plotly_chart(fig2, use_container_width=True)

# ── AI COMMENTARY ─────────────────────────────────────────────────────────────
elif page == "🎙️ AI Commentary":
    st.markdown('<p class="neon-title">🎙️ AI Match Commentary</p>', unsafe_allow_html=True)
    st.caption("Let the AI hype up any match result")
    st.divider()

    games = ["Free Fire MAX", "PUBG Mobile", "Call of Duty Mobile", "BGMI"]

    col1, col2 = st.columns(2)
    with col1:
        game = st.selectbox("Game", games)
        winner = st.selectbox("Winner", sorted(players_df["username"].tolist()))
        winner_kills = st.slider("Winner's kills", 0, 30, 12)
    with col2:
        prize_pool = st.number_input("Prize Pool (₹)", min_value=0, value=5000, step=500)
        st.markdown("**Top 5 Players**")
        top_players_input = []
        sample_players = sorted(players_df["username"].tolist())
        for i in range(1, 6):
            c1, c2, c3 = st.columns([2, 1, 1])
            with c1:
                p_name = st.selectbox(f"#{i}", sample_players, key=f"p{i}",
                                      index=min(i-1, len(sample_players)-1))
            with c2:
                p_kills = st.number_input("Kills", 0, 30, max(0, winner_kills - i*2), key=f"k{i}")
            with c3:
                st.write("")  # spacer
            top_players_input.append({"rank": i, "username": p_name, "kills": p_kills})

    st.divider()
    if st.button("🎙️ Generate Commentary", type="primary", use_container_width=True):
        match_data = {
            "game": game,
            "winner": winner,
            "winner_kills": winner_kills,
            "prize_pool": prize_pool,
            "top_players": top_players_input,
        }
        with st.spinner("AI is cooking up some hype..."):
            commentary = generate_match_commentary(match_data)
        st.markdown(f'<div class="commentary-box">🎙️ {commentary}</div>', unsafe_allow_html=True)
        st.balloons()
