"use client"

import { motion } from "framer-motion"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Smartphone, MessageCircle, User, Home, Sparkles } from "lucide-react"
import { useState } from "react"
import ScreenshotCarousel from "./screenshot-carousel"

const features = [
  {
    title: "Login & Registro",
    description: "Autenticación segura y proceso de registro intuitivo para comenzar rápidamente",
    image: "login.jpg",
    badge: "Seguridad",
    icon: User,
    color: "from-blue-500 to-blue-600"
  },
  {
    title: "Perfil de Usuario",
    description: "Personaliza tu perfil con foto y configuración de privacidad completa",
    image: "profile.jpg",
    badge: "Personalización",
    icon: User,
    color: "from-purple-500 to-purple-600"
  },
  {
    title: "Feed Principal",
    description: "Descubre contenido de usuarios que sigues con interacciones fluidas y naturales",
    image: "hero.jpg",
    badge: "Descubrimiento",
    icon: Home,
    color: "from-green-500 to-green-600"
  },
  {
    title: "Chat en Tiempo Real",
    description: "Mensajería instantánea",
    image: "messages.jpg",
    badge: "Comunicación",
    icon: MessageCircle,
    color: "from-orange-500 to-orange-600"
  },
  {
    title: "IA Comentarios",
    description: "Generación de comentarios con IA",
    image: "comments.jpg",
    badge: "Innovación",
    icon: Sparkles,
    color: "from-pink-500 to-pink-600"
  },
]

export default function Screenshots() {
  const [isCarouselOpen, setIsCarouselOpen] = useState(false)
  const [currentImageIndex, setCurrentImageIndex] = useState(0)

  const openCarousel = (index: number) => {
    setCurrentImageIndex(index)
    setIsCarouselOpen(true)
  }

  const closeCarousel = () => {
    setIsCarouselOpen(false)
  }

  const nextImage = () => {
    setCurrentImageIndex((prev) => (prev + 1) % features.length)
  }

  const previousImage = () => {
    setCurrentImageIndex((prev) => (prev - 1 + features.length) % features.length)
  }
  return (
    <section id="screenshots" className="py-24 px-4 bg-gradient-to-br from-orange-50 to-orange-100">
      <div className="container mx-auto">
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          whileInView={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8 }}
          viewport={{ once: true }}
          className="text-center mb-16"
        >
          <h2 className="text-4xl md:text-5xl font-bold mb-6 bg-gradient-to-r from-orange-600 to-orange-500 bg-clip-text text-transparent">
            Experiencia de Usuario
          </h2>
          <p className="text-xl text-gray-600 max-w-3xl mx-auto">
            Interfaz moderna e intuitiva diseñada con Jetpack Compose para Android
          </p>
        </motion.div>

        {/* Main Feature Showcase */}
        <div className="mb-16">
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            whileInView={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.8 }}
            viewport={{ once: true }}
            className="relative"
          >
            <Card className="overflow-hidden border-orange-200 shadow-2xl bg-gradient-to-br from-white to-orange-50">
              <CardContent className="p-8 md:p-12">
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
                  <div>
                    <Badge className="bg-orange-500 text-white mb-6 px-4 py-2 text-sm font-medium">
                      Destacado
                    </Badge>
                    <h3 className="text-3xl md:text-4xl font-bold mb-6 text-gray-800">
                      Feed Principal Inteligente
                    </h3>
                    <p className="text-lg text-gray-600 mb-8 leading-relaxed">
                      Descubre contenido personalizado de usuarios que sigues con un algoritmo 
                      inteligente que aprende de tus preferencias. Interacciones fluidas, 
                      carga rápida y experiencia optimizada.
                    </p>
                    <div className="grid grid-cols-2 gap-4">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center">
                          <Home className="w-5 h-5 text-green-600" />
                        </div>
                        <div>
                          <p className="font-semibold text-gray-800">Feed Personalizado</p>
                          <p className="text-sm text-gray-600">Contenido relevante</p>
                        </div>
                      </div>
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 bg-orange-100 rounded-full flex items-center justify-center">
                          <Sparkles className="w-5 h-5 text-orange-600" />
                        </div>
                        <div>
                          <p className="font-semibold text-gray-800">IA Integrada</p>
                          <p className="text-sm text-gray-600">Generación de comentarios con IA</p>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div className="relative flex justify-center">
                    <div className="relative" style={{ maxWidth: '280px' }}>
                      <img
                        src="hero.jpg"
                        alt="Feed Principal de Konnekt"
                        className="w-full h-auto rounded-3xl shadow-2xl cursor-pointer hover:scale-105 transition-transform duration-300"
                        style={{ aspectRatio: '9/16' }}
                        onClick={() => openCarousel(2)}
                      />
                      <div className="absolute -top-4 -right-4 w-16 h-16 bg-orange-500 rounded-full flex items-center justify-center shadow-lg">
                        <Smartphone className="w-6 h-6 text-white" />
                      </div>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </motion.div>
        </div>

        {/* Features Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          {features.filter((_, index) => index !== 2).map((feature, index) => {
            const IconComponent = feature.icon
            return (
              <motion.div
                key={index}
                initial={{ opacity: 0, y: 30 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6, delay: index * 0.1 }}
                viewport={{ once: true }}
                whileHover={{ y: -5, scale: 1.02 }}
                className="h-full cursor-pointer"
                onClick={() => openCarousel(index === 0 ? 0 : index === 1 ? 1 : index === 2 ? 3 : 4)}
              >
                <Card className="overflow-hidden border-orange-200 hover:border-orange-300 transition-all duration-300 hover:shadow-xl h-full group">
                  <CardContent className="p-0 h-full">
                    <div className="relative h-full flex flex-col">
                      <div className="absolute top-4 left-4 z-10">
                        <Badge className={`bg-gradient-to-r ${feature.color} text-white border-0`}>
                          {feature.badge}
                        </Badge>
                      </div>
                      <div className="absolute top-4 right-4 z-10">
                        <div className={`w-10 h-10 bg-gradient-to-r ${feature.color} rounded-full flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform duration-300`}>
                          <IconComponent className="w-5 h-5 text-white" />
                        </div>
                      </div>
                      {/* Contenedor de imagen con aspect ratio fijo para móviles */}
                      <div className="relative bg-gray-100 flex items-center justify-center" style={{ aspectRatio: '9/16', minHeight: '280px' }}>
                        <img
                          src={feature.image}
                          alt={feature.title}
                          className="w-full h-full object-contain group-hover:scale-105 transition-transform duration-300"
                        />
                      </div>
                      <div className="p-4 bg-white flex-grow">
                        <h3 className="text-lg font-semibold mb-2 text-gray-800 group-hover:text-orange-600 transition-colors duration-300">
                          {feature.title}
                        </h3>
                        <p className="text-sm text-gray-600 leading-relaxed">
                          {feature.description}
                        </p>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </motion.div>
            )
          })}
        </div>

        <ScreenshotCarousel
          screenshots={features}
          isOpen={isCarouselOpen}
          currentIndex={currentImageIndex}
          onClose={closeCarousel}
          onNext={nextImage}
          onPrevious={previousImage}
        />
      </div>
    </section>
  )
}
