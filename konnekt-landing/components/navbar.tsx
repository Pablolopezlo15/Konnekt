"use client"

import { motion } from "framer-motion"
import { Button } from "@/components/ui/button"
import { Download } from "lucide-react"
import Link from "next/link"
import { useEffect, useState } from "react"

export default function Navbar() {
  const [scrolled, setScrolled] = useState(false)

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 50)
    }

    window.addEventListener('scroll', handleScroll)
    return () => window.removeEventListener('scroll', handleScroll)
  }, [])

  return (
    <motion.nav
      initial={{ y: -100 }}
      animate={{ y: 0 }}
      transition={{ duration: 0.8 }}
      className={`fixed top-0 w-full z-50 border-b transition-all duration-300 ${
        scrolled 
          ? 'bg-white/60 backdrop-blur-xl border-orange-200/30 shadow-lg' 
          : 'bg-white/80 backdrop-blur-md border-orange-100/20 shadow-sm'
      }`}
    >
      <div className="container mx-auto px-4 py-4 flex items-center justify-between">
        <motion.div whileHover={{ scale: 1.05 }} className="flex items-center space-x-2">
          <img 
            src="/logo.png" 
            alt="Konnekt Logo" 
            className="w-10 h-10 rounded-xl"
          />
          <span className="text-2xl font-bold bg-gradient-to-r from-orange-600 to-orange-500 bg-clip-text text-transparent">
            Konnekt
          </span>
        </motion.div>

        <div className="hidden md:flex items-center space-x-8">
          <Link href="#features" className="text-gray-600 hover:text-orange-500 transition-colors">
            Características
          </Link>
          <Link href="#screenshots" className="text-gray-600 hover:text-orange-500 transition-colors">
            Experiencia
          </Link>
          <Link href="#tech" className="text-gray-600 hover:text-orange-500 transition-colors">
            Tecnologías
          </Link>
        </div>

        <div className="flex items-center space-x-4">
          <a
            href="https://drive.google.com/file/d/1c7hhzFf_HxUFwnPhWVu_KV4zJ2jYsNsl/view?usp=sharing"
            target="_blank"
            rel="noopener noreferrer"
          >
            <Button size="sm" className="bg-orange-500 hover:bg-orange-600">
              <Download className="w-4 h-4 mr-2" />
              Descargar
            </Button>
          </a>
        </div>
      </div>
    </motion.nav>
  )
}
