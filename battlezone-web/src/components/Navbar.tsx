'use client'
import Link from 'next/link'
import { useAuthStore } from '@/lib/store'
import { Trophy, Wallet, User, LogOut, Menu, X } from 'lucide-react'
import { useState } from 'react'

export default function Navbar() {
  const { user, logout } = useAuthStore()
  const [open, setOpen] = useState(false)

  return (
    <nav style={{ background: '#0d0d1a', borderBottom: '1px solid #1e1e3a' }} className="sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
        {/* Logo */}
        <Link href="/" className="text-2xl font-black neon-text">⚡ BattleZone</Link>

        {/* Desktop nav */}
        <div className="hidden md:flex items-center gap-6">
          <Link href="/tournaments" className="text-sm text-gray-400 hover:text-purple-400 transition-colors">Tournaments</Link>
          <Link href="/leaderboard" className="text-sm text-gray-400 hover:text-purple-400 transition-colors">Leaderboard</Link>
          <Link href="/live" className="text-sm text-red-400 hover:text-red-300 flex items-center gap-1 transition-colors">
            <span className="w-2 h-2 rounded-full bg-red-500 animate-pulse" />Live
          </Link>
          <a href="https://battlezone-esports-7nps6yjk8yf5xnjlnih85g.streamlit.app" target="_blank"
            className="text-sm text-purple-400 hover:text-purple-300 transition-colors">AI Analytics ↗</a>
        </div>

        {/* Right side */}
        <div className="hidden md:flex items-center gap-3">
          {user ? (
            <>
              <Link href="/wallet" className="flex items-center gap-2 bg-[#1a1a35] border border-[#2a2a4a] rounded-full px-3 py-1.5 text-sm text-purple-300">
                <Wallet size={14} /> ₹{user.wallet_balance?.toFixed(0) || 0}
              </Link>
              <Link href="/profile" className="flex items-center gap-2 bg-[#1a1a35] border border-[#2a2a4a] rounded-full px-3 py-1.5 text-sm text-gray-300">
                <User size={14} /> {user.username}
              </Link>
              <button onClick={logout} className="p-2 text-gray-500 hover:text-red-400 transition-colors">
                <LogOut size={16} />
              </button>
            </>
          ) : (
            <Link href="/auth" className="btn-primary text-sm px-4 py-2">Sign In</Link>
          )}
        </div>

        {/* Mobile menu button */}
        <button className="md:hidden text-gray-400" onClick={() => setOpen(!open)}>
          {open ? <X size={22} /> : <Menu size={22} />}
        </button>
      </div>

      {/* Mobile menu */}
      {open && (
        <div className="md:hidden px-4 pb-4 flex flex-col gap-3" style={{ background: '#0d0d1a' }}>
          <Link href="/tournaments" className="text-sm text-gray-400 py-2" onClick={() => setOpen(false)}>Tournaments</Link>
          <Link href="/leaderboard" className="text-sm text-gray-400 py-2" onClick={() => setOpen(false)}>Leaderboard</Link>
          <Link href="/live" className="text-sm text-red-400 py-2" onClick={() => setOpen(false)}>🔴 Live</Link>
          {user ? (
            <>
              <Link href="/profile" className="text-sm text-purple-300 py-2" onClick={() => setOpen(false)}>Profile — {user.username}</Link>
              <button onClick={() => { logout(); setOpen(false) }} className="text-sm text-red-400 text-left py-2">Sign Out</button>
            </>
          ) : (
            <Link href="/auth" className="btn-primary text-sm text-center py-2" onClick={() => setOpen(false)}>Sign In</Link>
          )}
        </div>
      )}
    </nav>
  )
}
