package com.filesystem

import com.Files.Directory

class State(val root: Directory, val wd: Directory, val output: String) {

def show : Unit = {
  println(output)
  print(State.SHELL_TOKEN)
}

  def setMessage(message: String): State = State(root,wd,message)

}


object State{

  def apply(root: Directory, wd: Directory,output: String = ""): State = new State(root,wd,output)

  val SHELL_TOKEN = "$ "
}