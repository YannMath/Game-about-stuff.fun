board:clearBoard()

local busStops = {}
local overviewTiles = {}

local function update()
    local players = getAllPlayers()

    for i = 0, math.min(players:size(), 4) - 1 do
        local player = players:get(i)
        local overviewTile = getTileByID(overviewTiles[i + 1])

        if player ~= nil and overviewTile ~= nil then
            overviewTile:setText(player:getMoney() .. "$")
            overviewTile:setColor(player:getColor())
        end
    end

    local nextPlayerTileJava = getTileByID(7)

    if nextPlayerTileJava ~= nil then
        nextPlayerTileJava:setColor(currentPlayer():getColor())
    end
end

local function getIndex(pos, array) 
    for i, arrayTile in ipairs(array) do
        if arrayTile.pos == pos then 
            return i
        end
    end
    return nil
end

local function getPreviousBusStop(position)
    for index, busStop in ipairs(busStops) do
        if busStop.pos == position then
            local previousIndex = (index - 2) % #busStops + 1
            return busStops[previousIndex]
        end
    end
    return nil
end

local function createEmptySpace()
    return tile:create(
        "EMPTY",
        0,
        0,
        "blumenstrasse.png",
        -1,
        -1
    )
end

local function createBlumenstrasse(name, boardPos)
    return {
        pos = boardPos,
        text = name .. " " .. boardPos,
        price = 100,
        rent = 15,
        imageLink = "blumenstrasse.png",
        onEnter = function(player)
            print("[LOG] " .. tostring(player:getName()) .. " entered the tile " .. name)
        end,
        onPass = function(player)
            --idk
        end
    }
end

local function createPlayerOverviewTile()
    return {
        isDisplay = true,
        buttonText = "Money not initiated",
        interaction = function(player, tile)
            --Do stuff
        end 
    }
end

local startTile = {
    pos = 0,
    text = "Start",
    price = 200,
    imageLink = "start.jpeg",
    buyable = false,
    onEnter = function(player)
        --Nothing here to look at
    end,
    onPass = function(player)
        player:reduceMoney(-200)
    end
}

local techUniEntTile = {
    pos = 8,
    text = "Technische Universität Entenhausen",
    price = 800,
    rent = 80,
    imageLink = "technische_uni_ent.png",
    onEnter = function(player)
        --Function not implemented yet
    end,
    onPass = function(player)
        player:reduceMoney(50)
        print("[LOG] " .. tostring(player:getName()) .. " contributed 50$ to technical research at the TUE")
    end
}

local busStop1 = {
    pos = 2,
    text = "Bushaltestelle",
    price = 150,
    imageLink = "bus_stop.jpeg",
    onEnter = function(player)
        local previousStop = getPreviousBusStop(player:getPos())
        player:reduceMoney(50)
        player:moveTo(previousStop.pos, 50)
        if getTileByPos(2):isOwned() then
            owner = getTileByPos(2):getOwner():reduceMoney(-50)
        end
    end
}

local busStop2 = {
    pos = 6,
    text = "Bushaltestelle",
    price = 150,
    imageLink = "bus_stop.jpeg",
    onEnter = function(player)
        local previousStop = getPreviousBusStop(player:getPos())
        player:reduceMoney(50)
        player:moveTo(previousStop.pos, 50)
        if getTileByPos(2):isOwned() then
            owner = getTileByPos(2):getOwner():reduceMoney(-50)
        end
    end
}

local busStop3 = {
    pos = 10,
    text = "Bushaltestelle",
    price = 150,
    imageLink = "bus_stop.jpeg",
    onEnter = function(player)
        local previousStop = getPreviousBusStop(player:getPos())
        player:reduceMoney(50)
        player:moveTo(previousStop.pos, 50)
        if getTileByPos(2):isOwned() then
            owner = getTileByPos(2):getOwner():reduceMoney(-50)
        end
    end
}

local busStop4 = {
    pos = 14,
    text = "Bushaltestelle",
    price = 150,
    imageLink = "bus_stop.jpeg",
    onEnter = function(player)
        local previousStop = getPreviousBusStop(player:getPos())
        player:reduceMoney(50)
        player:moveTo(previousStop.pos, 50)
        if getTileByPos(2):isOwned() then
            owner = getTileByPos(2):getOwner():reduceMoney(-50)
        end
    end
}

local diceTile = {
    isDisplay = true,
    buttonText = "Roll all dice",
    interaction = function(player, tile)
        local roll = rollAllDice()
        tile:setText("Rolled " .. tostring(roll))
        player:move(roll)
        update()
    end
}

local buyTile = {
    isDisplay = true,
    buttonText = "Buy current Tile",
    interaction = function(player, tile)
        player:buy()
        update()
    end
}

local nextPlayerTile = {
    isDisplay = true,
    buttonText = "Next Player",
    interaction = function(player, tile)
        nextPlayer()
        update()
    end
}

local blumenstrasse1 = createBlumenstrasse("Blumenstrasse", 1)
local blumenstrasse2 = createBlumenstrasse("Blumenstrasse", 2)
local blumenstrasse3 = createBlumenstrasse("Blumenstrasse", 3)
local blumenstrasse4 = createBlumenstrasse("Blumenstrasse", 4)
local blumenstrasse5 = createBlumenstrasse("Blumenstrasse", 5)
local blumenstrasse6 = createBlumenstrasse("Blumenstrasse", 6)
local blumenstrasse7 = createBlumenstrasse("Blumenstrasse", 7)
local blumenstrasse9 = createBlumenstrasse("Blumenstrasse", 9)
local blumenstrasse10= createBlumenstrasse("Blumenstrasse", 10)
local blumenstrasse11= createBlumenstrasse("Blumenstrasse", 11)
local blumenstrasse12= createBlumenstrasse("Blumenstrasse", 12)
local blumenstrasse13= createBlumenstrasse("Blumenstrasse", 13)
local blumenstrasse14= createBlumenstrasse("Blumenstrasse", 14)
local blumenstrasse15= createBlumenstrasse("Blumenstrasse", 15)

local player1OverviewTile = createPlayerOverviewTile()
local player2OverviewTile = createPlayerOverviewTile()
local player3OverviewTile = createPlayerOverviewTile()
local player4OverviewTile = createPlayerOverviewTile()

local boardLayout = {}
busStops = {busStop1, busStop2, busStop3, busStop4}
overviewTiles = {6, 8, 16, 18} 

for y = 1, 5 do
    boardLayout[y] = {}
    for x = 1, 5 do
        boardLayout[y][x] = createEmptySpace()
    end
end

boardLayout[5][5] = startTile
boardLayout[5][4] = blumenstrasse1
boardLayout[5][3] = busStop1
boardLayout[5][2] = blumenstrasse3
boardLayout[5][1] = blumenstrasse4
boardLayout[4][1] = blumenstrasse5
boardLayout[3][1] = busStop2
boardLayout[2][1] = blumenstrasse7
boardLayout[1][1] = techUniEntTile
boardLayout[1][2] = blumenstrasse9
boardLayout[1][3] = busStop3
boardLayout[1][4] = blumenstrasse11
boardLayout[1][5] = blumenstrasse12
boardLayout[2][5] = blumenstrasse13
boardLayout[3][5] = busStop4
boardLayout[4][5] = blumenstrasse15

boardLayout[2][3] = nextPlayerTile
boardLayout[3][3] = diceTile
boardLayout[4][3] = buyTile

boardLayout[2][2] = player1OverviewTile
boardLayout[2][4] = player2OverviewTile
boardLayout[4][2] = player3OverviewTile
boardLayout[4][4] = player4OverviewTile

board:loadBoard(boardLayout)