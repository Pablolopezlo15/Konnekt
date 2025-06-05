"use client"

import { motion } from "framer-motion"

const stats = [
  { number: "100%", label: "Nativo Android" },
  { number: "Real-time", label: "Mensajería" },
  { number: "Integración con IA", label: "Procesamiento" },
  { number: "2025", label: "Proyecto DAM" },
]

export default function Stats() {
  return (
    <section className="py-16 px-4 bg-gradient-to-r from-orange-500 to-orange-600">
      <div className="container mx-auto">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-8">
          {stats.map((stat, index) => (
            <motion.div
              key={index}
              initial={{ opacity: 0, y: 30 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: index * 0.1 }}
              viewport={{ once: true }}
              className="text-center text-white"
            >
              <div className="text-3xl md:text-5xl font-bold mb-3">{stat.number}</div>
              <div className="text-orange-100 text-sm md:text-base uppercase tracking-wider">{stat.label}</div>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  )
}
