'use client'
import { useEffect, useState } from 'react'
import api from '@/lib/api'

const TIER_COLORS: Record<string, string> = {
  Bronze: '#cd7f32', Silver: '#c0c0c0', Gold: '#ffd700',
  Platinum: '#00c8ff', Diamond: '#b9f2ff', Master: '#ff6ef7', Legend: '#ff4444',
}

export default function LeaderboardPage() {
  const [players, setPlayers] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [game, setGame] = useState('all')

  useEffect(() => {
    api.get(`/api/leaderboard/?game=${game}&limit=50`)
      .then(r => setPlayers(r.data.leaderboard || []))
      .catch(() => setPlayers(DEMO_PLAYERS))
      .finally(() => setLoading(false))
  }, [game])

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <h1 className="text-3xl font-black text-white mb-6">🏆 Leaderboard</h1>

      {/* Game filter */}
      <div className="flex gap-2 mb-6 flex-wrap">
        {['all', 'Free Fire MAX', 'PUBG Mobile', 'Call of Duty Mobile', 'BGMI'].map(g => (
          <button key={g} onClick={() => setGame(g)}
            className="px-3 py-1.5 rounded-full text-xs font-semibold transition-all capitalize"
            style={{
              background: game === g ? '#7c3aed' : '#12122a',
              color: game === g ? 'white' : '#888',
              border: `1px solid ${game === g ? '#7c3aed' : '#2a1a4a'}`,
            }}>
            {g === 'all' ? '🌍 Global' : g}
          </button>
        ))}
      </div>

      {/* Top 3 podium */}
      {!loading && players.length >= 3 && (
        <div className="grid grid-cols-3 gap-3 mb-6">
          {[players[1], players[0], players[2]].map((p, i) => {
            const actualRank = i === 0 ? 2 : i === 1 ? 1 : 3
            return (
              <div key={i} className={`card p-4 text-center ${actualRank === 1 ? 'border-[#ffd700]' : ''}`}
                style={{ borderColor: actualRank === 1 ? '#ffd700' : undefined }}>
                <div className="text-2xl">{actualRank === 1 ? '🥇' : actualRank === 2 ? '🥈' : '🥉'}</div>
                <div className="font-bold text-white text-sm mt-1 truncate">{p?.username}</div>
                <div className="text-xs mt-1" style={{ color: TIER_COLORS[p?.rank_tier] || '#888' }}>{p?.rank_tier}</div>
                <div className="text-purple-400 font-bold text-sm mt-1">{p?.stats?.rank_points} pts</div>
              </div>
            )
          })}
        </div>
      )}

      {/* Table */}
      <div className="card overflow-hidden">
        <table className="w-full">
          <thead>
            <tr style={{ background: '#1a1a35', borderBottom: '1px solid #2a2a4a' }}>
              <th className="text-left px-4 py-3 text-xs text-gray-500">#</th>
              <th className="text-left px-4 py-3 text-xs text-gray-500">Player</th>
              <th className="text-right px-4 py-3 text-xs text-gray-500">Rank Pts</th>
              <th className="text-right px-4 py-3 text-xs text-gray-500 hidden md:table-cell">Win Rate</th>
              <th className="text-right px-4 py-3 text-xs text-gray-500 hidden md:table-cell">Kills</th>
              <th className="text-right px-4 py-3 text-xs text-gray-500">Earnings</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              Array(10).fill(0).map((_, i) => (
                <tr key={i}><td colSpan={6} className="px-4 py-3"><div className="h-4 bg-[#1a1a35] rounded animate-pulse" /></td></tr>
              ))
            ) : players.map((p: any, i: number) => (
              <tr key={i} style={{ borderBottom: '1px solid #12122a' }}
                className="hover:bg-[#12122a] transition-colors">
                <td className="px-4 py-3 text-gray-500 text-sm">
                  {i < 3 ? ['🥇','🥈','🥉'][i] : `#${i+1}`}
                </td>
                <td className="px-4 py-3">
                  <div className="font-semibold text-white text-sm">{p.username}</div>
                  <div className="text-xs" style={{ color: TIER_COLORS[p.rank_tier] || '#888' }}>{p.rank_tier}</div>
                </td>
                <td className="px-4 py-3 text-right text-purple-400 font-bold text-sm">{p.stats?.rank_points}</td>
                <td className="px-4 py-3 text-right text-gray-400 text-sm hidden md:table-cell">{p.stats?.win_rate}%</td>
                <td className="px-4 py-3 text-right text-gray-400 text-sm hidden md:table-cell">{p.stats?.total_kills}</td>
                <td className="px-4 py-3 text-right text-[#f59e0b] text-sm font-semibold">₹{p.total_earnings?.toFixed(0)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

const DEMO_PLAYERS = Array(15).fill(0).map((_, i) => ({
  username: ['SnipeKing','BlazeShot','ProGamer99','DarkWolf','NightHunter','ShadowStrike','EliteForce','ThunderBolt','IronFist','CyberNinja','PixelWarrior','GhostRider','FireStorm','DeathBringer','LegendSlayer'][i],
  rank_tier: ['Legend','Master','Diamond','Diamond','Platinum','Platinum','Gold','Gold','Gold','Silver','Silver','Silver','Bronze','Bronze','Bronze'][i],
  stats: { rank_points: 15000 - i * 900, win_rate: 60 - i * 3, total_kills: 500 - i * 30 },
  total_earnings: 5000 - i * 300,
}))
