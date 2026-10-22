--[[ 
    This is a setup file. Be careful when editing it!!!
    This file MUST return an array of Lua dictionary style tables with the player profiles!
    Currently only the colors Cyan, Magenta, Orange and Green are 
]]

local players = {}

local player1 = {
    name = "Bobby da blob",
    spriteColor = color.MAGENTA,
    imageLink = "",
    autoImageLink = true
}

local player2 = {
    name = "Blobby",
    spriteColor = color.ORANGE,
    imageLink = "",
    autoImageLink = true
}

local player3 = {
    name = "Blobarus Blorarium",
    spriteColor = color.GREEN,
    imageLink = "",
    autoImageLink = true
}

local player4 = {
    name = "Bob the blob",
    spriteColor = color.CYAN,
    imageLink = "",
    autoImageLink = true
}

players[#players + 1] = player1
players[#players + 1] = player2
players[#players + 1] = player3
players[#players + 1] = player4

return players