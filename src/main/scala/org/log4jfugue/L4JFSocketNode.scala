package org.log4jfugue
/*
 * Log4JFugue - Application Sonification
 * Copyright (C) 2011-2012  Brian Tarbox
 *
 * http://www.log4jfugue.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
import org.scala_tools.subcut.inject.{Injectable, BindingModule}
import java.net.{SocketException, Socket}
import java.io.{IOException, EOFException, BufferedInputStream, ObjectInputStream}
import org.apache.log4j.spi._

/**
 *  This class is based on the log4j class SocketNode and we would have just
 *  extended it but the key member variables were defined as package private
 *  and so were not accessible to derived classes.
 *
 *  Read {@link LoggingEvent} objects sent from a remote client using
 *  Sockets (TCP).
 */
class L4JFSocketNode(socket: Socket)(override implicit val bindingModule: BindingModule) extends Runnable with Injectable {
  val messageProcessor = injectOptional[MessageProcessor].getOrElse(new MessageProcessor)
  
  override def run() {
    try {
      val ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream))
      while(true) {
        val event = ois.readObject() match {
          case x:LoggingEvent => messageProcessor ! x.getMessage()
          case _              => println("msg type error")
        }
      }
    } catch {
      case e:EOFException    => println("got EOF exception")
      case e:SocketException => println("got Socket exception")
      case e:IOException     => println("got IOException exception")
      case _                 => println("other exception")
    }
  }
}