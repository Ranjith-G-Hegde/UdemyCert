package com.commands
import com.Files.Directory
import com.filesystem.State

class MKdir(name: String) extends Command {

  def checkIllegal(name: String): Boolean = name.contains(".")

  def updateStructure(currentDirectory: Directory, path: List[String], newEntry: Directory):Directory = {

    if(path.isEmpty) currentDirectory.addEntry(newEntry)
    else {
      val oldEntry = currentDirectory.findEntry(path.head).asDirectory
    currentDirectory.replaceEntry(oldEntry.name,updateStructure(oldEntry, path.tail, newEntry))
    }
  }

  def doMkdir(state: State, name: String): State = {
    val wd = state.wd


    val allDirsInPath = wd.getAllFoldersInPath

    val newDir = Directory.empty(wd.path, name)

  val newRoot = updateStructure(state.root, allDirsInPath, newDir)

  val newWd = newRoot.findDescendant(allDirsInPath)

    State(newRoot, newWd )
  }

  override def apply(state: State): State = {
    val wd = state.wd
    if(wd.hasEntry(name))
    { state.setMessage("Entry " + name + " already exists!")}
   else if(name.contains(Directory.SEPARATOR)) {
      state.setMessage(name+"must not contain seperators!")
    }else if(checkIllegal(name)) {
        state.setMessage(name + ": illegal entry name!")
    } else{
      doMkdir(state,name)
    }
  }
}
















