"use client"

import { motion } from "framer-motion"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"

const technologies = [
  {
    category: "Frontend",
    color: "bg-blue-500",
    techs: ["Kotlin", "Jetpack Compose", "Android Studio"],
    icon: "🖥️",
  },
  {
    category: "Backend",
    color: "bg-green-500",
    techs: ["Python", "FastAPI", "WebSockets"],
    icon: "⚙️",
  },
  {
    category: "Base de Datos",
    color: "bg-purple-500",
    techs: ["MongoDB", "JSON Documents"],
    icon: "🗄️",
  },
  {
    category: "Inteligencia Artificial",
    color: "bg-orange-500",
    techs: ["Ollama", "minicpm-v", "Local Processing"],
    icon: "🤖",
  },
  {
    category: "Diseño",
    color: "bg-pink-500",
    techs: ["Figma", "UI/UX Design", "Prototyping"],
    icon: "🎨",
  },
  {
    category: "DevOps",
    color: "bg-gray-500",
    techs: ["Git", "GitHub", "Docker"],
    icon: "🚀",
  },
]

export default function TechStack() {
  return (
    <section id="tech" className="py-24 px-4 bg-white">
      <div className="container mx-auto">
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          whileInView={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8 }}
          viewport={{ once: true }}
          className="text-center mb-16"
        >
          <h2 className="text-4xl md:text-5xl font-bold mb-6 bg-gradient-to-r from-orange-600 to-orange-500 bg-clip-text text-transparent">
            Stack Tecnológico
          </h2>
          <p className="text-xl text-gray-600 max-w-3xl mx-auto">
            Tecnologías modernas y robustas para una experiencia excepcional
          </p>
        </motion.div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {technologies.map((tech, index) => (
            <motion.div
              key={index}
              initial={{ opacity: 0, y: 30 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: index * 0.1 }}
              viewport={{ once: true }}
              whileHover={{ y: -5 }}
            >
              <Card className="h-full border-orange-100 hover:border-orange-300 transition-all duration-300 hover:shadow-xl">
                <CardContent className="p-8">
                  <div className="flex items-center mb-6">
                    <div
                      className={`w-12 h-12 ${tech.color} rounded-xl mr-4 flex items-center justify-center text-white text-2xl`}
                    >
                      {tech.icon}
                    </div>
                    <h3 className="text-xl font-semibold text-gray-800">{tech.category}</h3>
                  </div>
                  <div className="flex flex-wrap gap-2">
                    {tech.techs.map((techName, techIndex) => (
                      <Badge
                        key={techIndex}
                        variant="secondary"
                        className="bg-orange-50 text-orange-700 hover:bg-orange-100 py-1.5"
                      >
                        {techName}
                      </Badge>
                    ))}
                  </div>
                </CardContent>
              </Card>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  )
}
