import streamlit as st
import plotly.express as px
import plotly.graph_objects as go
import pandas as pd
from analytics import load_data, top_players, game_stats, player_performance, predict_win_probability, tier_distribution
from ai_commentary import generate_match_commentary, generate_player_insight, generate_tournament_summary, chat_with_data

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
    .vs-box {
        background: linear-gradient(135deg, #1e1e3a, #2a1a4e);
        border: 2px solid #a855f7;
        border-radius: 12px;
        padding: 16px;
        text-align: center;
    }
    .win-badge {
        background: linear-gradient(135deg, #a855f7, #7c3aed);
        color: white;
        padding: 4px 12px;
        border-radius: 20px;
        font-weight: bold;
        font-size: 0.8rem;
    }
    div[data-testid="metric-container"] {
        background: linear-gradient(135deg, #1e1e3a, #2a1a4e);
        border: 1px solid #6c3fc5;
        border-radius: 10px;
        padding: 10px;
    }
    .chat-msg-user {
        background: #2a1a4e;
        border-radius: 12px 12px 2px 12px;
        padding: 10px 14px;
        margin: 6px 0;
        color: #e2d9f3;
        text-align: right;
    }
    .chat-msg-ai {
        background: #1a0a2e;
        border-left: 3px solid #a855f7;
        border-radius: 2px 12px 12px 12px;
        padding: 10px 14px;
        margin: 6px 0;
        color: #e2d9f3;
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
        st.warning("⚡ Demo mode — sample data.")
    else:
        st.success("✅ Live data connected")

    st.divider()
    page = st.radio("Navigate", [
        "🏠 Overview",
        "🔴 Live Tournament",
        "📝 Register for Tournament",
        "🏆 Leaderboard",
        "🔍 Player Scout",
        "⚔️ PvP Comparison",
        "🔮 Match Prediction",
        "🔥 Kill Heatmap",
        "📈 Streak Tracker",
        "💬 Chat with Data",
        "🎙️ AI Commentary",
    ])
    st.divider()
    st.caption("Powered by Groq LLaMA3 + BattleZone MongoDB")

# ── OVERVIEW ──────────────────────────────────────────────────────────────────
if page == "🏠 Overview":
    st.markdown('<p class="neon-title">⚡ BattleZone Analytics</p>', unsafe_allow_html=True)
    st.caption("Real-time esports intelligence powered by AI")
    st.divider()

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
                     color_discrete_sequence=colors, template="plotly_dark")
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

    st.subheader("📊 Game Performance Stats")
    gstats = game_stats(results_df)
    if not gstats.empty:
        st.dataframe(gstats, use_container_width=True)

    st.subheader("🤖 AI Tournament Summary")
    if st.button("Generate AI Summary", type="primary"):
        with st.spinner("AI is analysing the tournament..."):
            summary_data = gstats.to_string() if not gstats.empty else "No data"
            summary = generate_tournament_summary(summary_data)
            st.markdown(f'<div class="commentary-box">💬 {summary}</div>', unsafe_allow_html=True)

# ── LIVE TOURNAMENT ───────────────────────────────────────────────────────────
elif page == "🔴 Live Tournament":
    st.markdown('<p class="neon-title">🔴 Live Tournament</p>', unsafe_allow_html=True)
    st.caption("Watch the match live + real-time AI analytics side by side")
    st.divider()

    # ── Stream setup ──────────────────────────────────────────────────────────
    with st.expander("⚙️ Tournament Setup", expanded=True):
        col1, col2 = st.columns(2)
        with col1:
            tournament_name = st.text_input("Tournament Name", value="BattleZone Championship S1")
            game = st.selectbox("Game", ["Free Fire MAX", "PUBG Mobile", "Call of Duty Mobile", "BGMI"])
            prize_pool = st.number_input("Prize Pool (₹)", min_value=0, value=50000, step=1000)
        with col2:
            stream_platform = st.selectbox("Stream Platform", ["YouTube", "Twitch"])
            stream_url = st.text_input(
                "Stream URL or Video ID",
                placeholder="e.g. https://www.youtube.com/watch?v=dQw4w9WgXcQ or just the video ID",
                value=""
            )
            auto_refresh = st.toggle("Auto-refresh stats (30s)", value=True)

    st.divider()

    # ── Parse stream embed URL ────────────────────────────────────────────────
    def get_embed_url(url, platform):
        if not url:
            return None
        if platform == "YouTube":
            # Handle various YouTube URL formats
            if "youtube.com/watch?v=" in url:
                vid_id = url.split("v=")[1].split("&")[0]
            elif "youtu.be/" in url:
                vid_id = url.split("youtu.be/")[1].split("?")[0]
            elif "youtube.com/live/" in url:
                vid_id = url.split("youtube.com/live/")[1].split("?")[0]
            else:
                vid_id = url.strip()
            return f"https://www.youtube.com/embed/{vid_id}?autoplay=1&mute=0"
        elif platform == "Twitch":
            if "twitch.tv/" in url:
                channel = url.split("twitch.tv/")[1].split("/")[0]
            else:
                channel = url.strip()
            return f"https://player.twitch.tv/?channel={channel}&parent=battlezone-esports-7nps6yjk8yf5xnjlnih85g.streamlit.app&autoplay=true"
        return None

    embed_url = get_embed_url(stream_url, stream_platform)

    # ── Main layout: stream left, stats right ─────────────────────────────────
    col_stream, col_stats = st.columns([3, 2])

    with col_stream:
        st.markdown(f"### 🎮 {tournament_name}")
        st.markdown(f"**{game}** &nbsp;|&nbsp; 💰 Prize Pool: ₹{prize_pool:,}", unsafe_allow_html=True)

        if embed_url:
            # Embed the live stream
            st.markdown(
                f"""
                <div style="position:relative;padding-bottom:56.25%;height:0;overflow:hidden;border-radius:12px;border:2px solid #a855f7;">
                    <iframe
                        src="{embed_url}"
                        style="position:absolute;top:0;left:0;width:100%;height:100%;"
                        frameborder="0"
                        allowfullscreen
                        allow="autoplay; encrypted-media">
                    </iframe>
                </div>
                """,
                unsafe_allow_html=True
            )
        else:
            # Placeholder when no stream URL
            st.markdown(
                """
                <div style="
                    background: linear-gradient(135deg, #1e1e3a, #0e0e1a);
                    border: 2px dashed #a855f7;
                    border-radius: 12px;
                    height: 340px;
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    justify-content: center;
                    color: #6b7280;
                    font-size: 1.1rem;
                ">
                    <div style="font-size:3rem">📺</div>
                    <div style="margin-top:12px;color:#a855f7;font-weight:600">Paste a YouTube or Twitch URL above</div>
                    <div style="font-size:0.85rem;margin-top:6px">The live stream will appear here</div>
                </div>
                """,
                unsafe_allow_html=True
            )

        # Chat embed for YouTube
        if embed_url and stream_platform == "YouTube" and stream_url:
            vid_id = embed_url.split("/embed/")[1].split("?")[0]
            st.markdown("**💬 Live Chat**")
            st.markdown(
                f"""
                <iframe
                    src="https://www.youtube.com/live_chat?v={vid_id}&embed_domain=battlezone-esports-7nps6yjk8yf5xnjlnih85g.streamlit.app"
                    style="width:100%;height:300px;border-radius:8px;border:1px solid #6c3fc5;"
                    frameborder="0">
                </iframe>
                """,
                unsafe_allow_html=True
            )

    with col_stats:
        st.markdown("### 📊 Live Stats")

        if auto_refresh:
            import time
            st.caption(f"🔄 Auto-refreshing • Last updated: {pd.Timestamp.now().strftime('%H:%M:%S')}")

        # Live KPIs
        k1, k2, k3 = st.columns(3)
        k1.metric("👥 Players", len(players_df))
        k2.metric("⚔️ Total Kills", f"{players_df['total_kills'].sum():,}")
        k3.metric("🏆 Matches", results_df["match_id"].nunique() if not results_df.empty else 0)

        st.divider()

        # Live leaderboard — top 8
        st.markdown("**🏆 Live Leaderboard**")
        top8 = players_df.nlargest(8, "rank_points").reset_index(drop=True)
        tier_colors = {
            "Bronze": "🟤", "Silver": "⚪", "Gold": "🟡",
            "Platinum": "🔵", "Diamond": "💎", "Master": "🟣", "Legend": "🔴"
        }
        for i, row in top8.iterrows():
            rank_icon = ["🥇","🥈","🥉"][i] if i < 3 else f"#{i+1}"
            tier_icon = tier_colors.get(row["rank_tier"], "⚪")
            col_r, col_n, col_p = st.columns([1, 3, 2])
            col_r.markdown(f"**{rank_icon}**")
            col_n.markdown(f"{tier_icon} **{row['username']}**")
            col_p.markdown(f"`{int(row['rank_points'])} pts`")

        st.divider()

        # Kill chart - compact
        st.markdown("**⚔️ Top Killers**")
        top5_kills = players_df.nlargest(5, "total_kills")[["username", "total_kills"]]
        fig = px.bar(top5_kills, x="total_kills", y="username", orientation="h",
                     color="total_kills", color_continuous_scale="Purples",
                     template="plotly_dark")
        fig.update_layout(paper_bgcolor="rgba(0,0,0,0)", plot_bgcolor="rgba(0,0,0,0)",
                          margin=dict(t=5, b=5, l=5, r=5), height=200,
                          showlegend=False, coloraxis_showscale=False,
                          yaxis_title="", xaxis_title="Kills")
        st.plotly_chart(fig, use_container_width=True)

        st.divider()

        # AI live commentary
        st.markdown("**🤖 AI Live Commentary**")
        if st.button("🎙️ Generate Live Commentary", type="primary", use_container_width=True):
            top_player = players_df.nlargest(1, "rank_points").iloc[0]
            match_data = {
                "game": game,
                "winner": top_player["username"],
                "winner_kills": int(top_player["total_kills"] // max(top_player["matches_played"], 1)),
                "prize_pool": prize_pool,
                "top_players": [
                    {"rank": i+1, "username": row["username"],
                     "kills": int(row["total_kills"] // max(row["matches_played"], 1))}
                    for i, (_, row) in enumerate(players_df.nlargest(5, "rank_points").iterrows())
                ],
            }
            with st.spinner("AI is on the mic..."):
                commentary = generate_match_commentary(match_data)
            st.markdown(f'<div class="commentary-box">🎙️ {commentary}</div>', unsafe_allow_html=True)

        # Auto refresh
        if auto_refresh:
            time.sleep(30)
            st.rerun()

# ── REGISTER FOR TOURNAMENT ───────────────────────────────────────────────────
elif page == "📝 Register for Tournament":
    st.markdown('<p class="neon-title">📝 Register for Tournament</p>', unsafe_allow_html=True)
    st.caption("Pick your game, choose a tier, register your team and pay entry fee")
    st.divider()

    TIERS = {
        "🔰 Bronze — ₹25": {"fee": 25, "pool": 300, "prizes": [150, 90, 60], "color": "#cd7f32"},
        "⚔️ Silver — ₹50": {"fee": 50, "pool": 600, "prizes": [300, 180, 120], "color": "#c0c0c0"},
        "👑 Gold — ₹100":  {"fee": 100, "pool": 1200, "prizes": [600, 360, 240], "color": "#ffd700"},
    }
    GAMES = ["Free Fire MAX", "PUBG Mobile", "Call of Duty Mobile", "BGMI"]
    MODES = ["Solo", "Duo", "Squad"]

    # ── Step tracker ──────────────────────────────────────────────────────────
    if "reg_step" not in st.session_state:
        st.session_state.reg_step = 1
        st.session_state.reg_data = {}

    step = st.session_state.reg_step

    # Progress bar
    progress_labels = ["1. Select Game", "2. Choose Tier", "3. Team Details", "4. Payment", "5. Confirmed"]
    cols = st.columns(5)
    for i, label in enumerate(progress_labels):
        with cols[i]:
            if i + 1 < step:
                st.markdown(f"<div style='text-align:center;color:#7c3aed;font-size:0.75rem;font-weight:700'>✅ {label}</div>", unsafe_allow_html=True)
            elif i + 1 == step:
                st.markdown(f"<div style='text-align:center;color:#a855f7;font-size:0.75rem;font-weight:800;text-shadow:0 0 10px #a855f7'>{label}</div>", unsafe_allow_html=True)
            else:
                st.markdown(f"<div style='text-align:center;color:#374151;font-size:0.75rem'>{label}</div>", unsafe_allow_html=True)

    st.progress((step - 1) / 4)
    st.divider()

    # ── STEP 1: Select Game ───────────────────────────────────────────────────
    if step == 1:
        st.subheader("🎮 Select Your Game")
        game_emojis = {"Free Fire MAX": "🔥", "PUBG Mobile": "🎯", "Call of Duty Mobile": "💥", "BGMI": "⚔️"}

        col1, col2, col3, col4 = st.columns(4)
        cols_map = [col1, col2, col3, col4]
        selected_game = st.session_state.reg_data.get("game", "")

        for i, g in enumerate(GAMES):
            with cols_map[i]:
                is_sel = selected_game == g
                st.markdown(
                    f"""<div style='background:{"linear-gradient(135deg,#2a1a4e,#1e1e3a)" if is_sel else "#1e1e3a"};
                    border:2px solid {"#a855f7" if is_sel else "#2a2a4a"};border-radius:12px;
                    padding:16px;text-align:center;cursor:pointer;'>
                    <div style='font-size:2.5rem'>{game_emojis[g]}</div>
                    <div style='font-size:0.8rem;font-weight:700;color:{"#a855f7" if is_sel else "#9ca3af"};margin-top:8px'>{g}</div>
                    {"<div style='font-size:0.7rem;color:#10b981;margin-top:4px'>✓ Selected</div>" if is_sel else ""}
                    </div>""",
                    unsafe_allow_html=True
                )
                if st.button(f"Select" if not is_sel else "✓ Selected", key=f"game_{g}", use_container_width=True):
                    st.session_state.reg_data["game"] = g
                    st.rerun()

        st.divider()
        st.subheader("🕹️ Select Mode")
        mode = st.radio("Mode", MODES, horizontal=True, key="mode_select",
                        index=MODES.index(st.session_state.reg_data.get("mode", "Squad")))
        st.session_state.reg_data["mode"] = mode

        st.divider()
        if st.button("Next: Choose Tier →", type="primary", use_container_width=True):
            if not st.session_state.reg_data.get("game"):
                st.error("Please select a game first!")
            else:
                st.session_state.reg_step = 2
                st.rerun()

    # ── STEP 2: Choose Tier ───────────────────────────────────────────────────
    elif step == 2:
        g = st.session_state.reg_data.get("game", "")
        m = st.session_state.reg_data.get("mode", "")
        st.subheader(f"💰 Choose Your Tier — {g} {m}")

        for tier_name, tier in TIERS.items():
            is_sel = st.session_state.reg_data.get("tier") == tier_name
            with st.container():
                st.markdown(
                    f"""<div style='background:{"linear-gradient(135deg,#2a1a4e,#1e1e3a)" if is_sel else "#1e1e3a"};
                    border:2px solid {tier["color"] if is_sel else "#2a2a4a"};
                    border-radius:16px;padding:20px;margin-bottom:8px;'>
                    <div style='display:flex;justify-content:space-between;align-items:center;'>
                        <div>
                            <div style='font-size:1.2rem;font-weight:900;color:{tier["color"]}'>{tier_name}</div>
                            <div style='font-size:0.8rem;color:#6b7280;margin-top:4px'>Prize Pool: <span style='color:#f59e0b;font-weight:700'>₹{tier["pool"]}</span></div>
                        </div>
                        <div style='text-align:right'>
                            <div style='font-size:0.75rem;color:#6b7280'>Prizes</div>
                            <div style='font-size:0.8rem;color:#f59e0b'>🥇₹{tier["prizes"][0]} &nbsp;🥈₹{tier["prizes"][1]} &nbsp;🥉₹{tier["prizes"][2]}</div>
                        </div>
                    </div>
                    </div>""",
                    unsafe_allow_html=True
                )
                if st.button(f"{'✓ Selected' if is_sel else 'Choose'} {tier_name}", key=f"tier_{tier_name}", use_container_width=True):
                    st.session_state.reg_data["tier"] = tier_name
                    st.session_state.reg_data["tier_info"] = tier
                    st.rerun()

        st.divider()
        col1, col2 = st.columns(2)
        with col1:
            if st.button("← Back", use_container_width=True):
                st.session_state.reg_step = 1
                st.rerun()
        with col2:
            if st.button("Next: Team Details →", type="primary", use_container_width=True):
                if not st.session_state.reg_data.get("tier"):
                    st.error("Please select a tier!")
                else:
                    st.session_state.reg_step = 3
                    st.rerun()

    # ── STEP 3: Team Details ──────────────────────────────────────────────────
    elif step == 3:
        mode = st.session_state.reg_data.get("mode", "Squad")
        tier_info = st.session_state.reg_data.get("tier_info", {})
        tier_name = st.session_state.reg_data.get("tier", "")

        st.subheader("👥 Register Your Team")
        st.markdown(f"**{st.session_state.reg_data.get('game')}** • {mode} • "
                    f"<span style='color:{tier_info.get('color','#a855f7')}'>{tier_name}</span>",
                    unsafe_allow_html=True)
        st.divider()

        col1, col2 = st.columns(2)
        with col1:
            team_name = st.text_input("🏷️ Team Name *", placeholder="e.g. Team Alpha",
                                       value=st.session_state.reg_data.get("team_name", ""))
            player1 = st.text_input("🎮 Your In-Game Name (IGN) *", placeholder="Your username in game",
                                     value=st.session_state.reg_data.get("player1", ""))

        with col2:
            phone = st.text_input("📱 Phone Number *", placeholder="10-digit number",
                                   value=st.session_state.reg_data.get("phone", ""))
            email = st.text_input("📧 Email (optional)", placeholder="yourmail@gmail.com",
                                   value=st.session_state.reg_data.get("email", ""))

        if mode in ["Duo", "Squad"]:
            st.divider()
            st.subheader("👥 Team Members")
            player2 = st.text_input("Player 2 IGN *", value=st.session_state.reg_data.get("player2", ""))
        else:
            player2 = ""

        if mode == "Squad":
            player3 = st.text_input("Player 3 IGN *", value=st.session_state.reg_data.get("player3", ""))
            player4 = st.text_input("Player 4 IGN *", value=st.session_state.reg_data.get("player4", ""))
        else:
            player3 = player4 = ""

        st.divider()
        col1, col2 = st.columns(2)
        with col1:
            if st.button("← Back", use_container_width=True):
                st.session_state.reg_step = 2
                st.rerun()
        with col2:
            if st.button("Next: Payment →", type="primary", use_container_width=True):
                errors = []
                if not team_name.strip(): errors.append("Team name is required")
                if not player1.strip(): errors.append("Your IGN is required")
                if not phone.strip() or len(phone.strip()) < 10: errors.append("Valid phone number required")
                if mode in ["Duo", "Squad"] and not player2.strip(): errors.append("Player 2 IGN required")
                if mode == "Squad" and (not player3.strip() or not player4.strip()): errors.append("All squad members required")

                if errors:
                    for e in errors:
                        st.error(e)
                else:
                    st.session_state.reg_data.update({
                        "team_name": team_name, "player1": player1, "phone": phone,
                        "email": email, "player2": player2, "player3": player3, "player4": player4
                    })
                    st.session_state.reg_step = 4
                    st.rerun()

    # ── STEP 4: Payment ───────────────────────────────────────────────────────
    elif step == 4:
        d = st.session_state.reg_data
        tier_info = d.get("tier_info", {})
        tier_name = d.get("tier", "")
        fee = tier_info.get("fee", 0)
        prizes = tier_info.get("prizes", [0, 0, 0])
        color = tier_info.get("color", "#a855f7")

        st.subheader("💳 Confirm & Pay")

        col1, col2 = st.columns([3, 2])
        with col1:
            st.markdown("#### 📋 Order Summary")
            st.markdown(
                f"""<div style='background:#1e1e3a;border-radius:16px;padding:20px;'>
                <table style='width:100%;color:#e2d9f3;font-size:0.9rem;'>
                <tr><td style='color:#6b7280;padding:6px 0'>Game</td><td style='text-align:right;font-weight:700'>{d.get('game')}</td></tr>
                <tr><td style='color:#6b7280;padding:6px 0'>Mode</td><td style='text-align:right'>{d.get('mode')}</td></tr>
                <tr><td style='color:#6b7280;padding:6px 0'>Tier</td><td style='text-align:right;color:{color};font-weight:700'>{tier_name}</td></tr>
                <tr><td style='color:#6b7280;padding:6px 0'>Team</td><td style='text-align:right;font-weight:700'>{d.get('team_name')}</td></tr>
                <tr><td style='color:#6b7280;padding:6px 0'>Players</td><td style='text-align:right;font-size:0.8rem'>
                    {', '.join(filter(None, [d.get('player1'), d.get('player2'), d.get('player3'), d.get('player4')]))}</td></tr>
                <tr style='border-top:1px solid #2a2a4a'><td style='padding:10px 0 4px;font-weight:700'>Entry Fee</td>
                    <td style='text-align:right;font-size:1.4rem;font-weight:900;color:{color}'>₹{fee}</td></tr>
                </table></div>""",
                unsafe_allow_html=True
            )

        with col2:
            st.markdown("#### 🏆 You Could Win")
            st.markdown(
                f"""<div style='background:#0e0e1a;border:1px solid #1e1e3a;border-radius:16px;padding:16px;'>
                <div style='color:#6b7280;font-size:0.75rem;margin-bottom:12px'>Prize Pool: <span style='color:#f59e0b;font-weight:700'>₹{tier_info.get('pool',0)}</span></div>
                <div style='display:flex;justify-content:space-between;padding:6px 0;border-bottom:1px solid #1e1e3a'><span>🥇 1st</span><span style='color:#f59e0b;font-weight:700'>₹{prizes[0]}</span></div>
                <div style='display:flex;justify-content:space-between;padding:6px 0;border-bottom:1px solid #1e1e3a'><span>🥈 2nd</span><span style='color:#f59e0b;font-weight:700'>₹{prizes[1]}</span></div>
                <div style='display:flex;justify-content:space-between;padding:6px 0'><span>🥉 3rd</span><span style='color:#f59e0b;font-weight:700'>₹{prizes[2]}</span></div>
                </div>""",
                unsafe_allow_html=True
            )

            st.markdown("#### 💳 Payment Method")
            st.info("🔒 Secure payment via **Razorpay**\nUPI • Cards • Net Banking • Wallets")

        st.divider()

        # Razorpay button using HTML/JS
        rzp_key = "rzp_test_your_key_here"
        st.markdown(
            f"""
            <div style='text-align:center;padding:20px;'>
                <div id='rzp-button-container'>
                    <button onclick='openRazorpay()' style='
                        background:linear-gradient(135deg,#7c3aed,#5b21b6);
                        color:white;border:none;border-radius:12px;
                        padding:16px 48px;font-size:1.1rem;font-weight:800;
                        cursor:pointer;width:100%;max-width:400px;
                        box-shadow:0 8px 25px rgba(124,58,237,0.4);
                        transition:all 0.2s;'>
                        🔒 Pay ₹{fee} via Razorpay
                    </button>
                </div>
                <p style='color:#6b7280;font-size:0.75rem;margin-top:12px'>
                    🛡️ Slot confirmed only after successful payment
                </p>
            </div>
            <script src="https://checkout.razorpay.com/v1/checkout.js"></script>
            <script>
            function openRazorpay() {{
                var options = {{
                    key: '{rzp_key}',
                    amount: {fee * 100},
                    currency: 'INR',
                    name: 'BattleZone Esports',
                    description: '{tier_name} - {d.get("game", "")} {d.get("mode", "")}',
                    prefill: {{
                        contact: '{d.get("phone", "")}',
                        email: '{d.get("email", "") or "player@battlezone.gg"}'
                    }},
                    notes: {{
                        team_name: '{d.get("team_name", "")}',
                        game: '{d.get("game", "")}',
                        tier: '{tier_name}'
                    }},
                    theme: {{ color: '{color}' }},
                    handler: function(response) {{
                        document.getElementById('rzp-button-container').innerHTML =
                            '<div style="color:#10b981;font-size:1.2rem;font-weight:700;padding:20px;">✅ Payment Successful! Payment ID: ' + response.razorpay_payment_id + '</div>';
                        setTimeout(function() {{
                            window.parent.postMessage({{type:'payment_success',payment_id:response.razorpay_payment_id}}, '*');
                        }}, 1000);
                    }}
                }};
                var rzp = new Razorpay(options);
                rzp.open();
            }}
            </script>
            """,
            unsafe_allow_html=True
        )

        st.divider()
        col1, col2 = st.columns(2)
        with col1:
            if st.button("← Back", use_container_width=True):
                st.session_state.reg_step = 3
                st.rerun()
        with col2:
            if st.button("✅ Mark as Paid (Demo)", use_container_width=True, type="primary"):
                st.session_state.reg_step = 5
                st.rerun()

    # ── STEP 5: Confirmed ─────────────────────────────────────────────────────
    elif step == 5:
        d = st.session_state.reg_data
        tier_info = d.get("tier_info", {})
        color = tier_info.get("color", "#a855f7")

        st.markdown(
            f"""<div style='text-align:center;padding:40px 20px;'>
            <div style='font-size:5rem;margin-bottom:16px'>🎉</div>
            <div style='font-size:2.5rem;font-weight:900;color:white;margin-bottom:8px'>You're In!</div>
            <div style='color:#6b7280;font-size:1rem;margin-bottom:32px'>Your slot has been confirmed. Get ready to battle!</div>
            </div>""",
            unsafe_allow_html=True
        )

        # Ticket
        players_list = ', '.join(filter(None, [d.get('player1'), d.get('player2'), d.get('player3'), d.get('player4')]))
        st.markdown(
            f"""<div style='background:linear-gradient(135deg,#1a0a2e,#0e1a2e);
            border:2px solid {color};border-radius:20px;padding:24px;
            max-width:500px;margin:0 auto 32px;
            box-shadow:0 0 40px {color}44;'>
            <div style='display:flex;justify-content:space-between;align-items:center;margin-bottom:16px'>
                <div>
                    <div style='font-size:0.7rem;color:{color};font-weight:700;letter-spacing:2px'>TOURNAMENT TICKET</div>
                    <div style='font-size:1.4rem;font-weight:900;color:white;margin-top:4px'>{d.get("game")}</div>
                    <div style='color:#6b7280;font-size:0.85rem'>{d.get("mode")} • {d.get("tier","")}</div>
                </div>
                <div style='font-size:2.5rem'>🎮</div>
            </div>
            <div style='border-top:1px dashed #2a2a4a;border-bottom:1px dashed #2a2a4a;padding:12px 0;margin:12px 0;'>
            <table style='width:100%;color:#e2d9f3;font-size:0.85rem;'>
            <tr><td style='color:#6b7280;padding:4px 0'>Team</td><td style='text-align:right;font-weight:700'>{d.get("team_name")}</td></tr>
            <tr><td style='color:#6b7280;padding:4px 0'>Players</td><td style='text-align:right;font-size:0.8rem'>{players_list}</td></tr>
            <tr><td style='color:#6b7280;padding:4px 0'>Entry Paid</td><td style='text-align:right;color:#10b981;font-weight:700'>₹{tier_info.get("fee",0)} ✅</td></tr>
            <tr><td style='color:#6b7280;padding:4px 0'>Prize Pool</td><td style='text-align:right;color:#f59e0b;font-weight:700'>₹{tier_info.get("pool",0)}</td></tr>
            </table></div>
            <div style='text-align:center;color:#374151;font-size:0.75rem;margin-top:12px'>
            🔐 Room ID will be shared 15 min before match start on your phone
            </div></div>""",
            unsafe_allow_html=True
        )

        col1, col2 = st.columns(2)
        with col1:
            if st.button("🎮 Register Another Team", use_container_width=True):
                st.session_state.reg_step = 1
                st.session_state.reg_data = {}
                st.rerun()
        with col2:
            if st.button("🏠 Go to Overview", type="primary", use_container_width=True):
                st.session_state.reg_step = 1
                st.session_state.reg_data = {}
                st.rerun()

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

    def medal(i):
        return ["🥇", "🥈", "🥉"][i] if i < 3 else f"#{i+1}"

    top_df.insert(0, "Rank", [medal(i) for i in range(len(top_df))])
    st.dataframe(
        top_df[["Rank", "username", "rank_tier", "rank_points", "win_rate", "total_kills", "total_earnings", "level"]],
        use_container_width=True, hide_index=True,
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
            fig.update_layout(paper_bgcolor="rgba(0,0,0,0)", font={"color": "#e2d9f3"},
                              height=280, margin=dict(t=20, b=20))
            st.plotly_chart(fig, use_container_width=True)

        st.divider()
        st.subheader("🤖 AI Player Insight")
        if st.button("Generate AI Insight", type="primary"):
            if perf:
                with st.spinner(f"Analysing {selected}..."):
                    insight = generate_player_insight(perf, selected)
                    st.markdown(f'<div class="commentary-box">💬 {insight}</div>', unsafe_allow_html=True)
            else:
                st.warning("Need match history to generate insight.")

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

# ── PVP COMPARISON ────────────────────────────────────────────────────────────
elif page == "⚔️ PvP Comparison":
    st.markdown('<p class="neon-title">⚔️ Player vs Player</p>', unsafe_allow_html=True)
    st.caption("Head-to-head stats comparison with AI verdict")
    st.divider()

    usernames = sorted(players_df["username"].tolist())
    col1, col_vs, col2 = st.columns([2, 1, 2])
    with col1:
        p1 = st.selectbox("🔵 Player 1", usernames, index=0)
    with col_vs:
        st.markdown("<br><br>", unsafe_allow_html=True)
        st.markdown('<div style="text-align:center;font-size:2rem;color:#a855f7;font-weight:900">VS</div>', unsafe_allow_html=True)
    with col2:
        p2 = st.selectbox("🔴 Player 2", usernames, index=1)

    if p1 == p2:
        st.warning("Select two different players!")
    else:
        r1 = players_df[players_df["username"] == p1].iloc[0]
        r2 = players_df[players_df["username"] == p2].iloc[0]
        perf1 = player_performance(results_df, p1)
        perf2 = player_performance(results_df, p2)
        prob1 = predict_win_probability(players_df, p1) or 0
        prob2 = predict_win_probability(players_df, p2) or 0

        st.divider()

        # Side by side stats
        metrics = [
            ("🏆 Rank Points", int(r1["rank_points"]), int(r2["rank_points"])),
            ("📊 Win Rate %", r1["win_rate"], r2["win_rate"]),
            ("⚔️ Total Kills", int(r1["total_kills"]), int(r2["total_kills"])),
            ("🎮 Matches Played", int(r1["matches_played"]), int(r2["matches_played"])),
            ("⭐ Level", int(r1["level"]), int(r2["level"])),
            ("🎯 Win Probability", prob1, prob2),
        ]

        for label, v1, v2 in metrics:
            c1, cm, c2 = st.columns([2, 1, 2])
            winner_side = "left" if v1 > v2 else ("right" if v2 > v1 else "tie")
            with c1:
                badge = "🏅" if winner_side == "left" else ""
                st.metric(f"{p1} {badge}", v1)
            with cm:
                st.markdown(f"<div style='text-align:center;padding-top:20px;color:#6b7280;font-size:0.85rem'>{label}</div>", unsafe_allow_html=True)
            with c2:
                badge = "🏅" if winner_side == "right" else ""
                st.metric(f"{p2} {badge}", v2)

        # Radar chart
        st.divider()
        st.subheader("📡 Stat Radar")
        max_rp = players_df["rank_points"].max() or 1
        max_kills = players_df["total_kills"].max() or 1
        max_matches = players_df["matches_played"].max() or 1

        categories = ["Win Rate", "Rank Points", "Kills", "Matches", "Win Prob"]
        vals1 = [
            r1["win_rate"],
            r1["rank_points"] / max_rp * 100,
            r1["total_kills"] / max_kills * 100,
            r1["matches_played"] / max_matches * 100,
            prob1,
        ]
        vals2 = [
            r2["win_rate"],
            r2["rank_points"] / max_rp * 100,
            r2["total_kills"] / max_kills * 100,
            r2["matches_played"] / max_matches * 100,
            prob2,
        ]

        fig = go.Figure()
        fig.add_trace(go.Scatterpolar(r=vals1 + [vals1[0]], theta=categories + [categories[0]],
                                      fill='toself', name=p1, line_color="#a855f7"))
        fig.add_trace(go.Scatterpolar(r=vals2 + [vals2[0]], theta=categories + [categories[0]],
                                      fill='toself', name=p2, line_color="#f43f5e"))
        fig.update_layout(polar=dict(bgcolor="#1e1e3a",
                          radialaxis=dict(visible=True, range=[0, 100], color="#6b7280")),
                          paper_bgcolor="rgba(0,0,0,0)", font=dict(color="#e2d9f3"),
                          legend=dict(bgcolor="rgba(0,0,0,0)"))
        st.plotly_chart(fig, use_container_width=True)

        # AI verdict
        st.divider()
        st.subheader("🤖 AI Verdict")
        if st.button("Who would win? Ask AI", type="primary"):
            prompt_data = {
                p1: {"rank_points": int(r1["rank_points"]), "win_rate": r1["win_rate"],
                     "total_kills": int(r1["total_kills"]), "win_probability": prob1,
                     "rank_tier": r1["rank_tier"]},
                p2: {"rank_points": int(r2["rank_points"]), "win_rate": r2["win_rate"],
                     "total_kills": int(r2["total_kills"]), "win_probability": prob2,
                     "rank_tier": r2["rank_tier"]},
            }
            with st.spinner("AI is analysing the matchup..."):
                verdict = chat_with_data(
                    f"Compare these two esports players and predict who would win in a 1v1 match. Be confident, pick a winner, explain why in 3 sentences max. Stats: {prompt_data}",
                    players_df, results_df
                )
            st.markdown(f'<div class="commentary-box">🤖 {verdict}</div>', unsafe_allow_html=True)

# ── MATCH PREDICTION ──────────────────────────────────────────────────────────
elif page == "🔮 Match Prediction":
    st.markdown('<p class="neon-title">🔮 Match Prediction</p>', unsafe_allow_html=True)
    st.caption("AI predicts match outcome before it happens")
    st.divider()

    usernames = sorted(players_df["username"].tolist())
    games = ["Free Fire MAX", "PUBG Mobile", "Call of Duty Mobile", "BGMI"]

    col1, col2 = st.columns(2)
    with col1:
        game = st.selectbox("Game", games)
        selected_players = st.multiselect("Select players (pick 4-10)", usernames,
                                           default=usernames[:6])
    with col2:
        prize_pool = st.number_input("Prize Pool (₹)", min_value=100, value=5000, step=500)
        st.markdown("**Selected players:**")
        if selected_players:
            for p in selected_players:
                row = players_df[players_df["username"] == p]
                if not row.empty:
                    tier = row.iloc[0]["rank_tier"]
                    prob = predict_win_probability(players_df, p) or 0
                    st.markdown(f"• **{p}** — {tier} — {prob}% win chance")

    st.divider()
    if st.button("🔮 Predict Match", type="primary", use_container_width=True):
        if len(selected_players) < 2:
            st.warning("Select at least 2 players!")
        else:
            player_stats = {}
            for p in selected_players:
                row = players_df[players_df["username"] == p]
                if not row.empty:
                    player_stats[p] = {
                        "rank_tier": row.iloc[0]["rank_tier"],
                        "win_rate": row.iloc[0]["win_rate"],
                        "rank_points": int(row.iloc[0]["rank_points"]),
                        "win_probability": predict_win_probability(players_df, p) or 0,
                    }

            # Show probability bars
            st.subheader("📊 Win Probability per Player")
            prob_df = pd.DataFrame([
                {"Player": p, "Win Probability %": s["win_probability"], "Tier": s["rank_tier"]}
                for p, s in player_stats.items()
            ]).sort_values("Win Probability %", ascending=False)

            fig = px.bar(prob_df, x="Player", y="Win Probability %", color="Tier",
                         template="plotly_dark",
                         color_discrete_map={
                             "Bronze":"#cd7f32","Silver":"#c0c0c0","Gold":"#ffd700",
                             "Platinum":"#00c8ff","Diamond":"#b9f2ff","Master":"#ff6ef7","Legend":"#ff4444"
                         })
            fig.update_layout(paper_bgcolor="rgba(0,0,0,0)", plot_bgcolor="rgba(0,0,0,0)")
            st.plotly_chart(fig, use_container_width=True)

            with st.spinner("AI is predicting the match..."):
                prediction = chat_with_data(
                    f"Predict the outcome of a {game} match with these players. Pick a winner and top 3. Be specific and hype. Stats: {player_stats}. Prize Pool: ₹{prize_pool}",
                    players_df, results_df
                )
            st.markdown(f'<div class="commentary-box">🔮 {prediction}</div>', unsafe_allow_html=True)

# ── KILL HEATMAP ──────────────────────────────────────────────────────────────
elif page == "🔥 Kill Heatmap":
    st.markdown('<p class="neon-title">🔥 Kill Heatmap</p>', unsafe_allow_html=True)
    st.caption("Who dominates which game")
    st.divider()

    if results_df.empty:
        st.info("No match data available.")
    else:
        top_n = st.slider("Show top N players", 5, 15, 10)
        top_usernames = players_df.nlargest(top_n, "total_kills")["username"].tolist()
        filtered = results_df[results_df["username"].isin(top_usernames)]

        if not filtered.empty:
            heatmap_df = filtered.groupby(["username", "game"])["kills"].sum().reset_index()
            pivot = heatmap_df.pivot(index="username", columns="game", values="kills").fillna(0)

            fig = px.imshow(pivot, color_continuous_scale="Purples",
                            template="plotly_dark", aspect="auto",
                            labels=dict(x="Game", y="Player", color="Total Kills"))
            fig.update_layout(paper_bgcolor="rgba(0,0,0,0)", plot_bgcolor="rgba(0,0,0,0)",
                              margin=dict(t=30))
            st.plotly_chart(fig, use_container_width=True)

            st.divider()
            st.subheader("🏆 Top Killer per Game")
            cols = st.columns(len(pivot.columns))
            for i, game in enumerate(pivot.columns):
                top_killer = pivot[game].idxmax()
                kills = int(pivot[game].max())
                with cols[i]:
                    st.metric(game, top_killer, f"{kills} kills")

# ── STREAK TRACKER ────────────────────────────────────────────────────────────
elif page == "📈 Streak Tracker":
    st.markdown('<p class="neon-title">📈 Streak & Form</p>', unsafe_allow_html=True)
    st.caption("Player momentum — who's hot and who's cold")
    st.divider()

    if results_df.empty:
        st.info("No match data available.")
    else:
        usernames = sorted(players_df["username"].tolist())
        selected = st.selectbox("Select player", usernames)

        player_matches = results_df[results_df["username"] == selected].copy()

        if player_matches.empty:
            st.info("No match history for this player.")
        else:
            player_matches = player_matches.reset_index(drop=True)
            player_matches["match_num"] = range(1, len(player_matches) + 1)
            player_matches["won"] = (player_matches["rank"] == 1).astype(int)
            player_matches["rolling_kills"] = player_matches["kills"].rolling(3, min_periods=1).mean().round(2)

            col1, col2, col3 = st.columns(3)
            recent = player_matches.tail(5)
            wins_recent = int(recent["won"].sum())
            avg_kills_recent = round(recent["kills"].mean(), 1)
            best_streak = 0
            cur = 0
            for w in player_matches["won"]:
                cur = cur + 1 if w else 0
                best_streak = max(best_streak, cur)

            col1.metric("🔥 Recent Wins (last 5)", f"{wins_recent}/5")
            col2.metric("⚔️ Avg Kills (last 5)", avg_kills_recent)
            col3.metric("🏆 Best Win Streak", best_streak)

            st.divider()
            col_l, col_r = st.columns(2)

            with col_l:
                st.subheader("📊 Kill Trend")
                fig = go.Figure()
                fig.add_trace(go.Scatter(x=player_matches["match_num"], y=player_matches["kills"],
                                         mode="markers+lines", name="Kills", line_color="#a855f7",
                                         marker=dict(size=8)))
                fig.add_trace(go.Scatter(x=player_matches["match_num"], y=player_matches["rolling_kills"],
                                         mode="lines", name="3-match avg", line=dict(color="#f43f5e", dash="dash")))
                fig.update_layout(paper_bgcolor="rgba(0,0,0,0)", plot_bgcolor="rgba(0,0,0,0)",
                                  template="plotly_dark", xaxis_title="Match #", yaxis_title="Kills")
                st.plotly_chart(fig, use_container_width=True)

            with col_r:
                st.subheader("🏅 Rank Trend")
                fig2 = go.Figure()
                fig2.add_trace(go.Scatter(x=player_matches["match_num"], y=player_matches["rank"],
                                          mode="markers+lines", name="Rank", line_color="#00c8ff",
                                          marker=dict(size=8)))
                fig2.update_layout(paper_bgcolor="rgba(0,0,0,0)", plot_bgcolor="rgba(0,0,0,0)",
                                   template="plotly_dark", xaxis_title="Match #", yaxis_title="Rank",
                                   yaxis=dict(autorange="reversed"))
                st.plotly_chart(fig2, use_container_width=True)

            # Form indicator
            st.divider()
            st.subheader("🌡️ Current Form")
            form_icons = []
            for _, row in player_matches.tail(5).iterrows():
                if row["rank"] == 1:
                    form_icons.append("🟢 W")
                elif row["rank"] <= 3:
                    form_icons.append("🟡 T")
                else:
                    form_icons.append("🔴 L")
            st.markdown("**Last 5 matches:** " + "  →  ".join(form_icons))

# ── CHAT WITH DATA ────────────────────────────────────────────────────────────
elif page == "💬 Chat with Data":
    st.markdown('<p class="neon-title">💬 Chat with Data</p>', unsafe_allow_html=True)
    st.caption("Ask anything about your tournament data")
    st.divider()

    if "chat_history" not in st.session_state:
        st.session_state.chat_history = []

    # Example questions
    st.markdown("**Try asking:**")
    examples = [
        "Who has the best KD ratio?",
        "Which game has the most kills?",
        "Who is the top earner?",
        "Which player is on the best form?",
        "Who should I watch out for in PUBG Mobile?",
    ]
    cols = st.columns(len(examples))
    for i, ex in enumerate(examples):
        if cols[i].button(ex, key=f"ex_{i}"):
            st.session_state.chat_history.append({"role": "user", "content": ex})
            with st.spinner("Thinking..."):
                answer = chat_with_data(ex, players_df, results_df)
            st.session_state.chat_history.append({"role": "ai", "content": answer})

    st.divider()

    # Chat history
    for msg in st.session_state.chat_history:
        if msg["role"] == "user":
            st.markdown(f'<div class="chat-msg-user">🧑 {msg["content"]}</div>', unsafe_allow_html=True)
        else:
            st.markdown(f'<div class="chat-msg-ai">🤖 {msg["content"]}</div>', unsafe_allow_html=True)

    # Input
    with st.form("chat_form", clear_on_submit=True):
        col1, col2 = st.columns([5, 1])
        with col1:
            user_input = st.text_input("Ask about your data...", label_visibility="collapsed",
                                        placeholder="e.g. Who has the highest win rate in BGMI?")
        with col2:
            submitted = st.form_submit_button("Send 🚀", use_container_width=True)

    if submitted and user_input:
        st.session_state.chat_history.append({"role": "user", "content": user_input})
        with st.spinner("AI is thinking..."):
            answer = chat_with_data(user_input, players_df, results_df)
        st.session_state.chat_history.append({"role": "ai", "content": answer})
        st.rerun()

    if st.session_state.chat_history:
        if st.button("🗑️ Clear chat"):
            st.session_state.chat_history = []
            st.rerun()

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
                st.write("")
            top_players_input.append({"rank": i, "username": p_name, "kills": p_kills})

    st.divider()
    if st.button("🎙️ Generate Commentary", type="primary", use_container_width=True):
        match_data = {
            "game": game, "winner": winner, "winner_kills": winner_kills,
            "prize_pool": prize_pool, "top_players": top_players_input,
        }
        with st.spinner("AI is cooking up some hype..."):
            commentary = generate_match_commentary(match_data)
        st.markdown(f'<div class="commentary-box">🎙️ {commentary}</div>', unsafe_allow_html=True)
        st.balloons()
