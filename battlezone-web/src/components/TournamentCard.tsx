'use client'
import Link from 'next/link'
import { Users, Trophy, Clock } from 'lucide-react'

interface Tournament {
  match_id: string
  title: string
  game: string
  mode: string
  entry_fee: number
  prize_pool: number
  max_players: number
  current_players: number
  start_time: string
  status: 'upcoming' | 'live' | 'completed' | 'cancelled'
  is_featured?: boolean
  banner_url?: string
}

export default function TournamentCard({ t }: { t: Tournament }) {
  const fillPct = Math.round((t.current_players / t.max_players) * 100)
  const startDate = new Date(t.start_time)

  const gameColors: Record<string, string> = {
    'Free Fire MAX': '#ff6b35',
    'PUBG Mobile': '#f59e0b',
    'Call of Duty Mobile': '#10b981',
    'Battlegrounds Mobile India': '#3b82f6',
    'BGMI': '#3b82f6',
  }
  const color = gameColors[t.game] || '#7c3aed'

  return (
    <Link href={`/tournaments/${t.match_id}`}>
      <div className="card p-4 cursor-pointer hover:-translate-y-1 transition-all duration-200 hover:shadow-[0_8px_30px_rgba(124,58,237,0.3)]">
        {/* Header */}
        <div className="flex justify-between items-start mb-3">
          <div>
            <div className="font-bold text-base text-[#e0e0ff]">{t.title}</div>
            <div className="text-xs mt-0.5" style={{ color }}>{t.game} • {t.mode}</div>
          </div>
          <span className={`badge-${t.status === 'live' ? 'live' : t.status === 'completed' ? 'completed' : 'upcoming'}`}>
            {t.status === 'live' ? '🔴 LIVE' : t.status === 'completed' ? '✅ Done' : '⏳ Soon'}
          </span>
        </div>

        {/* Prize + Fee */}
        <div className="flex justify-between items-center mb-3">
          <div>
            <div className="text-xs text-gray-500">Prize Pool</div>
            <div className="text-xl font-black text-[#f59e0b]">₹{t.prize_pool.toLocaleString()}</div>
          </div>
          <div className="text-right">
            <div className="text-xs text-gray-500">Entry Fee</div>
            <div className="text-base font-bold text-purple-400">
              {t.entry_fee === 0 ? '🆓 Free' : `₹${t.entry_fee}`}
            </div>
          </div>
        </div>

        {/* Slots progress */}
        <div className="mb-3">
          <div className="flex justify-between text-xs text-gray-500 mb-1">
            <span className="flex items-center gap-1"><Users size={11} /> {t.current_players}/{t.max_players} players</span>
            <span>{fillPct}% full</span>
          </div>
          <div className="bg-[#1e1e3a] rounded-full h-1.5">
            <div className="h-full rounded-full transition-all"
              style={{ width: `${fillPct}%`, background: `linear-gradient(90deg, ${color}, #7c3aed)` }} />
          </div>
        </div>

        {/* Time */}
        <div className="flex items-center gap-1.5 text-xs text-gray-500">
          <Clock size={11} />
          {startDate.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' })}
        </div>
      </div>
    </Link>
  )
}
