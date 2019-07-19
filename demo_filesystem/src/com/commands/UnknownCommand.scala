package com.commands
import com.filesystem.State

class UnknownCommand extends Command {
  override def apply(state: State): State = state.setMessage("Command Not Found!")



}
