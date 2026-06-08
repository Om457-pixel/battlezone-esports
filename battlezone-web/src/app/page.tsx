'use client'
import Link from 'next/link'
import { useEffect, useState } from 'react'
import api from '@/lib/api'
import TournamentCard from '@/components/TournamentCard'
import { Trophy, Users, Zap, Shield } from 'lucide-react'

export default function Home() {
  const [featured, setFeatured] = useState<any[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get('/api/matches/featured').then(r => setFeatured(r.data.matches || []))
      .catch(() => setFeatured(DEMO_TOURNAMENTS))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div>
      {/* Hero */}
      <section className="relative overflow-hidden px-4 pt-20 pb-16 text-center"
        style={{ background: 'radial-gradient(ellipse at 50% 0%, rgba(124,58,237,0.15) 0%, transparent 70%)' }}>
        <div className="max-w-3xl mx-auto">
          <div className="inline-block badge-live mb-4">🔴 Season 1 Live Now</div>
          <h1 className="text-5xl md:text-7xl font-black mb-4 leading-tight">
            <span className="neon-text">BattleZone</span>
            <br />
            <span className="text-white text-3xl md:text-4xl font-bold">Esports Tournament Platform</span>
          </h1>
          <p className="text-gray-400 text-lg mb-8 max-w-xl mx-auto">
            Host & join competitive tournaments for Free Fire MAX, PUBG Mobile, COD Mobile and BGMI.
            Win real cash prizes.
          </p>
          <div className="flex flex-wrap gap-3 justify-center">
            <Link href="/tournaments" className="btn-primary px-8 py-3 text-base">
              🎮 Browse Tournaments
            </Link>
            <Link href="/auth" className="px-8 py-3 text-base rounded-xl border border-purple-600 text-purple-400 hover:bg-purple-900/20 transition-colors">
              Join Free →
            </Link>
          </div>
        </div>

        {/* Stats row */}
        <div className="mt-16 grid grid-cols-2 md:grid-cols-4 gap-4 max-w-4xl mx-auto">
          {[
            { icon: <Trophy size={24} />, val: '₹50K+', label: 'Prize Pool' },
            { icon: <Users size={24} />, val: '2000+', label: 'Players' },
            { icon: <Zap size={24} />, val: '500+', label: 'Matches' },
            { icon: <Shield size={24} />, val: '100%', label: 'Secure Payments' },
          ].map((s, i) => (
            <div key={i} className="card p-4 text-center">
              <div className="text-purple-400 flex justify-center mb-2">{s.icon}</div>
              <div className="text-2xl font-black text-white">{s.val}</div>
              <div className="text-xs text-gray-500 mt-1">{s.label}</div>
            </div>
          ))}
        </div>
      </section>

      {/* Featured tournaments */}
      <section className="max-w-7xl mx-auto px-4 py-12">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-2xl font-bold text-white">🔥 Featured Tournaments</h2>
          <Link href="/tournaments" className="text-sm text-purple-400 hover:text-purple-300">View all →</Link>
        </div>

        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {[1,2,3].map(i => (
              <div key={i} className="card p-4 animate-pulse h-48" />
            ))}
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {featured.map((t: any) => <TournamentCard key={t.match_id} t={t} />)}
          </div>
        )}
      </section>

      {/* How it works */}
      <section className="max-w-5xl mx-auto px-4 py-12">
        <h2 className="text-2xl font-bold text-white text-center mb-10">How It Works</h2>
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          {[
            { step: '01', title: 'Sign Up', desc: 'Register with your phone number via OTP', icon: '📱' },
            { step: '02', title: 'Find Tournament', desc: 'Browse upcoming matches by game and prize pool', icon: '🔍' },
            { step: '03', title: 'Pay & Register', desc: 'Pay entry fee securely via Razorpay, get your slot', icon: '💳' },
            { step: '04', title: 'Play & Win', desc: 'Submit results and get prize directly in wallet', icon: '🏆' },
          ].map((s, i) => (
            <div key={i} className="card p-5 text-center">
              <div className="text-3xl mb-3">{s.icon}</div>
              <div className="text-xs text-purple-400 font-bold mb-1">STEP {s.step}</div>
              <div className="font-bold text-white mb-2">{s.title}</div>
              <div className="text-xs text-gray-500">{s.desc}</div>
            </div>
          ))}
        </div>
      </section>

      {/* Games */}
      <section className="max-w-5xl mx-auto px-4 pb-16">
        <h2 className="text-2xl font-bold text-white text-center mb-8">Supported Games</h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {[
            { name: 'Free Fire MAX', color: '#ff6b35', emoji: '🔥' },
            { name: 'PUBG Mobile', color: '#f59e0b', emoji: '🎯' },
            { name: 'COD Mobile', color: '#10b981', emoji: '💥' },
            { name: 'BGMI', color: '#3b82f6', emoji: '⚔️' },
          ].map((g, i) => (
            <Link href={`/tournaments?game=${g.name}`} key={i}>
              <div className="card p-5 text-center hover:-translate-y-1 transition-all cursor-pointer"
                style={{ borderColor: g.color + '44' }}>
                <div className="text-4xl mb-2">{g.emoji}</div>
                <div className="text-sm font-bold" style={{ color: g.color }}>{g.name}</div>
              </div>
            </Link>
          ))}
        </div>
      </section>
    </div>
  )
}

const DEMO_TOURNAMENTS = [
  { match_id: '1', title: 'Friday Night Showdown', game: 'Free Fire MAX', mode: 'Squad', entry_fee: 50, prize_pool: 5000, max_players: 48, current_players: 32, start_time: new Date(Date.now() + 86400000).toISOString(), status: 'upcoming' as const },
  { match_id: '2', title: 'PUBG Pro League', game: 'PUBG Mobile', mode: 'Squad', entry_fee: 100, prize_pool: 10000, max_players: 64, current_players: 60, start_time: new Date(Date.now() + 3600000).toISOString(), status: 'live' as const },
  { match_id: '3', title: 'COD Weekend Cup', game: 'Call of Duty Mobile', mode: 'Squad', entry_fee: 0, prize_pool: 2000, max_players: 32, current_players: 18, start_time: new Date(Date.now() + 172800000).toISOString(), status: 'upcoming' as const },
]
