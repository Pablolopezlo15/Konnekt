"use client"

import { motion } from "framer-motion"
import { Card, CardContent } from "@/components/ui/card"
import { MessageCircle, Heart, Users, Bot, Camera, Shield, Zap, Palette } from "lucide-react"

const features = [
  {
    icon: Camera,
    title: "Publicaciones Visuales",
    description: "Comparte tus momentos favoritos con imágenes de alta calidad y descripciones personalizadas.",
  },
  {
    icon: Bot,
    title: "IA Integrada",
    description: "Genera comentarios automáticos con nuestro modelo de IA local minicpm-v usando Ollama.",
  },
  {
    icon: MessageCircle,
    title: "Chat en Tiempo Real",
    description: "Mensajería instantánea con WebSockets para conversaciones fluidas y naturales.",
  },
  {
    icon: Users,
    title: "Red Social Completa",
    description: "Sistema de seguidores, likes, comentarios y guardado de publicaciones favoritas.",
  },
  {
    icon: Shield,
    title: "Privacidad Avanzada",
    description: "Control total sobre tu privacidad con cuentas públicas y privadas.",
  },
  {
    icon: Zap,
    title: "Rendimiento Nativo",
    description: "Desarrollado en Kotlin con Jetpack Compose para máximo rendimiento en Android.",
  },
  {
    icon: Palette,
    title: "Diseño Adaptativo",
    description: "Tema claro y oscuro automático que se adapta a las preferencias del sistema.",
  },
  {
    icon: Heart,
    title: "Interacciones Intuitivas",
    description: "Like con doble tap, comentarios, guardado y todas las funciones que esperas.",
  },
]

export default function Features() {
  return (
    <section className="py-24 px-4 bg-white">
      <div className="container mx-auto">
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          whileInView={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8 }}
          viewport={{ once: true }}
          className="text-center mb-16"
        >
          <h2 className="text-4xl md:text-5xl font-bold mb-6 bg-gradient-to-r from-orange-600 to-orange-500 bg-clip-text text-transparent">
            Características Innovadoras
          </h2>
          <p className="text-xl text-gray-600 max-w-3xl mx-auto">
            Konnekt combina lo mejor de las redes sociales tradicionales con tecnología de vanguardia
          </p>
        </motion.div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          {features.map((feature, index) => (
            <motion.div
              key={index}
              initial={{ opacity: 0, y: 30 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: index * 0.1 }}
              viewport={{ once: true }}
              whileHover={{ y: -5 }}
            >
              <Card className="h-full border-orange-100 hover:border-orange-300 transition-all duration-300 hover:shadow-xl overflow-hidden">
                <CardContent className="p-8 text-center">
                  <div className="w-16 h-16 bg-gradient-to-br from-orange-500 to-orange-600 rounded-2xl flex items-center justify-center mx-auto mb-6">
                    <feature.icon className="w-8 h-8 text-white" />
                  </div>
                  <h3 className="text-xl font-semibold mb-3 text-gray-800">{feature.title}</h3>
                  <p className="text-gray-600 leading-relaxed">{feature.description}</p>
                </CardContent>
              </Card>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  )
}
