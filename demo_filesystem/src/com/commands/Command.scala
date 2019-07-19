package com.commands

import com.filesystem.State

trait Command {


def apply(state: State): State

}


object Command {

  val MKDIR = "mkdir"
   val LS = "ls"
  def emptyCommand: Command = new Command {
    override def apply(state: State): State = state
  }

  def incompleteCommand(name: String): Command = new Command {
    override def apply(state: State): State = state.setMessage(name + ":Incomplete Command!!")
  }

  def from(input: String): Command = {

    val tokens = input.split(" ")
    if (tokens.isEmpty || input.isEmpty) emptyCommand
    else if (MKDIR.equals(tokens(0))) {
      if (tokens.length < 2) incompleteCommand(MKDIR)
      else new MKdir(tokens(1))
    }else if(LS.equals(tokens(0))){
      new Ls
    } else new UnknownCommand
  }
}





