'use client'
import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import api from '@/lib/api'
import { useAuthStore } from '@/lib/store'
import { Trophy, Zap, Target, Star, Copy } from 'lucide-react'
import toast from 'react-hot-toast'

const TIER_COLORS: Record<string, string> = {
  Bronze: '#cd7f32', Silver: '#c0c0c0', Gold: '#ffd700',
  Platinum: '#00c8ff', Diamond: '#b9f2ff', Master: '#ff6ef7', Legend: '#ff4444',
}

const TIER_ORDER = ['Bronze','Silver','Gold','Platinum','Diamond','Master','Legend']

export default function ProfilePage() {
  const { user, token, setUser } = useAuthStore()
  const router = useRouter()
  const [matches, setMatches] = useState<any[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!token) { router.push('/auth'); return }
    api.get('/api/users/me').then(r => setUser(r.data)).catch(() => {})
    api.get('/api/matches/?status=completed&limit=10')
      .then(r => setMatches(r.data.matches || []))
      .catch(() => setMatches([]))
      .finally(() => setLoading(false))
  }, [token])

  if (!user) return null

  const tierIdx = TIER_ORDER.indexOf(user.rank_tier)
  const nextTier = TIER_ORDER[tierIdx + 1]
  const tierColor = TIER_COLORS[user.rank_tier] || '#888'
  const xpPct = user.level ? Math.min(((user as any).xp / (user as any).xp_to_next_level) * 100, 100) : 0

  const copyReferral = () => {
    navigator.clipboard.writeText((user as any).referral_code || 'BZONE123')
    toast.success('Referral code copied!')
  }

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      {/* Profile header */}
      <div className="card p-6 mb-6"
        style={{ background: 'linear-gradient(135deg, #1a0a2e, #0e1a2e)', borderColor: '#3b1a6a' }}>
        <div className="flex flex-wrap gap-6 items-start">
          {/* Avatar */}
          <div className="w-20 h-20 rounded-2xl flex items-center justify-center text-4xl font-black"
            style={{ background: `linear-gradient(135deg, ${tierColor}44, ${tierColor}22)`, border: `2px solid ${tierColor}` }}>
            {user.username?.[0]?.toUpperCase() || '?'}
          </div>

          {/* Info */}
          <div className="flex-1">
            <div className="flex items-center gap-3 mb-1">
              <h1 className="text-2xl font-black text-white">{user.username}</h1>
              <span className="text-sm font-bold px-3 py-1 rounded-full"
                style={{ background: `${tierColor}22`, color: tierColor, border: `1px solid ${tierColor}44` }}>
                {user.rank_tier}
              </span>
            </div>
            <div className="text-gray-500 text-sm mb-3">Level {user.level} • {user.phone}</div>

            {/* XP bar */}
            <div className="max-w-xs">
              <div className="flex justify-between text-xs text-gray-500 mb-1">
                <span>Level {user.level}</span>
                <span>{(user as any).xp || 0} / {(user as any).xp_to_next_level || 100} XP</span>
              </div>
              <div className="bg-[#1e1e3a] rounded-full h-2">
                <div className="h-full rounded-full transition-all"
                  style={{ width: `${xpPct}%`, background: `linear-gradient(90deg, ${tierColor}, #7c3aed)` }} />
              </div>
              {nextTier && <div className="text-xs text-gray-600 mt-1">Next: {nextTier}</div>}
            </div>
          </div>

          {/* Wallet snapshot */}
          <div className="text-right">
            <div className="text-xs text-gray-500 mb-1">Wallet Balance</div>
            <div className="text-3xl font-black text-[#f59e0b]">₹{user.wallet_balance?.toFixed(0)}</div>
            <div className="text-xs text-green-400 mt-1">+₹{user.bonus_balance?.toFixed(0)} bonus</div>
          </div>
        </div>
      </div>

      {/* Stats row */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
        {[
          { icon: <Trophy size={20} />, label: 'Matches Played', value: user.stats?.matches_played || 0, color: '#7c3aed' },
          { icon: <Star size={20} />, label: 'Wins', value: user.stats?.matches_won || 0, color: '#f59e0b' },
          { icon: <Target size={20} />, label: 'Total Kills', value: user.stats?.total_kills || 0, color: '#ef4444' },
          { icon: <Zap size={20} />, label: 'Win Rate', value: `${user.stats?.win_rate?.toFixed(1) || 0}%`, color: '#10b981' },
        ].map((s, i) => (
          <div key={i} className="card p-4">
            <div className="mb-2" style={{ color: s.color }}>{s.icon}</div>
            <div className="text-2xl font-black text-white">{s.value}</div>
            <div className="text-xs text-gray-500 mt-1">{s.label}</div>
          </div>
        ))}
      </div>

      <div className="grid md:grid-cols-3 gap-6">
        {/* Rank card */}
        <div className="card p-5">
          <h2 className="font-bold text-white mb-4">🏅 Rank</h2>
          <div className="text-center py-4">
            <div className="text-6xl font-black mb-2" style={{ color: tierColor }}>{user.rank_tier}</div>
            <div className="text-gray-400 text-sm">{user.stats?.rank_points || 0} rank points</div>
            {nextTier && (
              <div className="mt-4 text-xs text-gray-600">
                Keep playing to reach <span style={{ color: TIER_COLORS[nextTier] }}>{nextTier}</span>
              </div>
            )}
          </div>
          {/* Tier ladder */}
          <div className="space-y-1.5 mt-2">
            {TIER_ORDER.map((t, i) => (
              <div key={t} className="flex items-center gap-2">
                <div className="w-2 h-2 rounded-full" style={{ background: TIER_COLORS[t], opacity: i <= tierIdx ? 1 : 0.2 }} />
                <span className="text-xs" style={{ color: i <= tierIdx ? TIER_COLORS[t] : '#333', fontWeight: i === tierIdx ? 700 : 400 }}>
                  {t} {i === tierIdx ? '← you' : ''}
                </span>
              </div>
            ))}
          </div>
        </div>

        {/* Achievements */}
        <div className="card p-5">
          <h2 className="font-bold text-white mb-4">🏆 Achievements</h2>
          {((user as any).achievements?.length > 0) ? (
            <div className="space-y-2">
              {(user as any).achievements.map((a: any, i: number) => (
                <div key={i} className="flex items-center gap-2 bg-[#1a1a35] rounded-lg p-2 text-sm">
                  <span>{a.icon || '🎖️'}</span>
                  <span className="text-gray-300">{a.name}</span>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-8 text-gray-600">
              <div className="text-3xl mb-2">🎖️</div>
              <div className="text-sm">Play matches to earn achievements</div>
            </div>
          )}
        </div>

        {/* Referral */}
        <div className="card p-5">
          <h2 className="font-bold text-white mb-4">🎁 Referral</h2>
          <p className="text-gray-500 text-sm mb-4">Earn ₹50 bonus for every friend who joins!</p>
          <div className="bg-[#1a1a35] rounded-xl p-4 text-center">
            <div className="text-2xl font-black text-purple-400 tracking-widest mb-2">
              {(user as any).referral_code || 'BZONE123'}
            </div>
            <button onClick={copyReferral}
              className="flex items-center gap-2 mx-auto text-xs text-gray-400 hover:text-purple-400 transition-colors">
              <Copy size={12} /> Copy code
            </button>
          </div>
          <div className="mt-4 text-xs text-gray-600 text-center">
            Share this code with friends
          </div>
        </div>
      </div>

      {/* Recent matches */}
      <div className="card p-5 mt-6">
        <h2 className="font-bold text-white mb-4">📋 Recent Matches</h2>
        {loading ? (
          <div className="space-y-2">
            {[1,2,3].map(i => <div key={i} className="h-12 bg-[#1a1a35] rounded-lg animate-pulse" />)}
          </div>
        ) : matches.length === 0 ? (
          <div className="text-center py-8 text-gray-600">
            <div className="text-3xl mb-2">🎮</div>
            <div className="text-sm">No matches played yet</div>
          </div>
        ) : (
          <div className="space-y-2">
            {matches.map((m: any, i: number) => (
              <div key={i} className="flex justify-between items-center bg-[#1a1a35] rounded-lg px-4 py-3">
                <div>
                  <div className="text-sm font-semibold text-white">{m.title}</div>
                  <div className="text-xs text-gray-500">{m.game}</div>
                </div>
                <div className="text-right">
                  <div className="text-xs text-gray-500">{new Date(m.start_time).toLocaleDateString('en-IN')}</div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
