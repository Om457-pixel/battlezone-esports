'use client'
import { useSearchParams } from 'next/navigation'
import { Suspense, useEffect, useState } from 'react'

declare global { interface Window { Razorpay: any } }

function PayContent() {
  const searchParams = useSearchParams()
  const [paid, setPaid] = useState(false)
  const [paymentId, setPaymentId] = useState('')
  const [loading, setLoading] = useState(false)

  const fee = parseInt(searchParams.get('fee') || '25')
  const tierName = searchParams.get('tier') || 'Bronze Tier'
  const game = searchParams.get('game') || 'Free Fire MAX'
  const mode = searchParams.get('mode') || 'Squad'
  const teamName = searchParams.get('team') || ''
  const p1 = searchParams.get('p1') || ''
  const p2 = searchParams.get('p2') || ''
  const p3 = searchParams.get('p3') || ''
  const p4 = searchParams.get('p4') || ''
  const phone = searchParams.get('phone') || ''
  const email = searchParams.get('email') || 'player@battlezone.gg'

  const tierColors: Record<string, string> = { '25': '#cd7f32', '50': '#c0c0c0', '100': '#ffd700' }
  const tierPrizes: Record<string, number[]> = {
    '25': [150, 90, 60], '50': [300, 180, 120], '100': [600, 360, 240]
  }
  const color = tierColors[String(fee)] || '#a855f7'
  const prizes = tierPrizes[String(fee)] || [0, 0, 0]
  const prizePool = prizes.reduce((a, b) => a + b, 0)
  const players = [p1, p2, p3, p4].filter(Boolean).join(', ')

  useEffect(() => {
    const script = document.createElement('script')
    script.src = 'https://checkout.razorpay.com/v1/checkout.js'
    document.body.appendChild(script)
    return () => { document.body.removeChild(script) }
  }, [])

  const handlePay = () => {
    setLoading(true)
    const options = {
      key: process.env.NEXT_PUBLIC_RAZORPAY_KEY_ID,
      amount: fee * 100,
      currency: 'INR',
      name: 'BattleZone Esports',
      image: 'https://raw.githubusercontent.com/Om457-pixel/battlezone-esports/main/battlezone-web/public/logo.png',
      description: `${game} ${mode} Tournament`,
      prefill: { contact: phone, email },
      notes: { team_name: teamName, game, mode, tier: tierName },
      theme: { color },
      handler: (response: any) => {
        setPaymentId(response.razorpay_payment_id)
        setPaid(true)
        setLoading(false)
      },
      modal: { ondismiss: () => setLoading(false) },
    }
    try {
      new window.Razorpay(options).open()
    } catch {
      setLoading(false)
      alert('Payment failed to open. Please try again.')
    }
  }

  if (paid) return (
    <div className="min-h-screen flex items-center justify-center px-4"
      style={{ background: 'radial-gradient(ellipse at 50% 0%, rgba(16,185,129,0.1) 0%, #0a0a0f 70%)' }}>
      <div className="card p-8 w-full max-w-md text-center">
        <div className="text-6xl mb-4">🎉</div>
        <h2 className="text-2xl font-black text-white mb-2">Slot Confirmed!</h2>
        <p className="text-gray-500 mb-6">Payment successful. You're in!</p>

        {/* Ticket */}
        <div className="text-left rounded-xl p-5 mb-4"
          style={{ background: '#0e0e1a', border: `2px solid ${color}`, boxShadow: `0 0 30px ${color}44` }}>
          <div className="text-xs font-bold tracking-widest mb-3" style={{ color }}>TOURNAMENT TICKET</div>
          {[
            ['Game', `${game} — ${mode}`],
            ['Tier', tierName],
            ['Team', teamName],
            ['Players', players],
            ['Entry Paid', `₹${fee} ✅`],
            ['Prize Pool', `₹${prizePool}`],
          ].map(([label, value]) => (
            <div key={label} className="flex justify-between py-1.5 border-b border-[#1e1e3a] last:border-0 text-sm">
              <span className="text-gray-500">{label}</span>
              <span className="font-semibold text-white">{value}</span>
            </div>
          ))}
          <p className="text-xs text-gray-600 text-center mt-4">Payment ID: {paymentId}</p>
        </div>

        <p className="text-xs text-gray-600 mb-6">
          🔐 Room ID will be shared on your phone 15 min before match start
        </p>
        <button onClick={() => window.close()} className="btn-primary w-full py-3">
          Close & Return
        </button>
      </div>
    </div>
  )

  return (
    <div className="min-h-screen flex items-center justify-center px-4"
      style={{ background: 'radial-gradient(ellipse at 50% 0%, rgba(124,58,237,0.1) 0%, #0a0a0f 70%)' }}>
      <div className="card p-8 w-full max-w-md">
        <div className="text-2xl font-black neon-text mb-1">⚡ BattleZone</div>
        <p className="text-gray-500 text-sm mb-6">Secure Tournament Payment</p>

        <h2 className="text-xl font-bold text-white mb-4">💳 Confirm & Pay</h2>

        {/* Order summary */}
        <div className="rounded-xl p-4 mb-4" style={{ background: '#0e0e1a' }}>
          {[
            ['Game', game],
            ['Mode', mode],
            ['Tier', tierName],
            ['Team', teamName],
            ['Players', players],
          ].map(([label, value]) => (
            <div key={label} className="flex justify-between py-1.5 border-b border-[#1e1e3a] last:border-0 text-sm">
              <span className="text-gray-500">{label}</span>
              <span className="font-semibold text-white">{value}</span>
            </div>
          ))}
          <div className="flex justify-between items-center pt-3 mt-1">
            <span className="font-bold text-white">Entry Fee</span>
            <span className="text-3xl font-black" style={{ color }}>₹{fee}</span>
          </div>
        </div>

        {/* Prizes */}
        <div className="rounded-xl p-4 mb-6" style={{ background: '#0e0e1a' }}>
          <div className="text-xs text-gray-500 mb-3">
            🏆 Prize Pool: <span className="text-[#f59e0b] font-bold">₹{prizePool}</span>
          </div>
          {[['🥇 1st', prizes[0]], ['🥈 2nd', prizes[1]], ['🥉 3rd', prizes[2]]].map(([label, amt]) => (
            <div key={String(label)} className="flex justify-between text-sm py-1">
              <span className="text-gray-400">{label}</span>
              <span className="font-bold text-[#f59e0b]">₹{amt}</span>
            </div>
          ))}
        </div>

        <button onClick={handlePay} disabled={loading} className="btn-primary w-full py-4 text-base mb-3">
          {loading ? '⏳ Opening payment...' : `🔒 Pay ₹${fee} via Razorpay`}
        </button>
        <p className="text-xs text-gray-600 text-center">
          🛡️ UPI • Debit/Credit Cards • Net Banking • Wallets
        </p>
      </div>
    </div>
  )
}

export default function PayPage() {
  return (
    <Suspense fallback={<div className="min-h-screen flex items-center justify-center text-gray-400">Loading...</div>}>
      <PayContent />
    </Suspense>
  )
}
