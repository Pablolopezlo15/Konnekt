"use client"
import Navbar from "@/components/navbar"
import Hero from "@/components/hero"
import Features from "@/components/features"
import Screenshots from "@/components/screenshots"
import TechStack from "@/components/tech-stack"
import Stats from "@/components/stats"
import CTA from "@/components/cta"
import Footer from "@/components/footer"

export default function Home() {
  return (
    <div className="min-h-screen bg-white">
      <Navbar />
      <Hero />
      <Stats />
      <Features />
      <Screenshots />
      <TechStack />
      <CTA />
      <Footer />
    </div>
  )
}
