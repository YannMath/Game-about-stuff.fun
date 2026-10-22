--[[ 
    This is a setup file. Be careful when editing it!!!
    This file MUST return a Lua dictionary style table with the important game configurations!
]]

setup = {
    amountOfExtraPlayers = 0, --These are players that are not initialized in the config_player.lua file and are usually 0
    startMoney = 2000,
    randomStartPos = false,
    aiActive = false,
    dice = {6, 6},
    boardSize = 16
}

return setup