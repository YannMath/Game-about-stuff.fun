board:clearBoard()

local function createEmptySpace()
    return tile:create(
        "EMPTY",
        0,
        "blumenstrasse.png",
        -1
    )
end

local function createBlumenstrasse(name, boardPos)
    return {
        pos = boardPos,
        text = name .. " " .. boardPos,
        price = 100,
        imageLink = "blumenstrasse.png",
        onEnter = function(player)
            print(tostring(player) .. " entered the tile " .. name)
        end
    }
end

local startTile = {
    pos = 0,
    text = "Start",
    price = 0,
    imageLink = "start.jpeg",
    onEnter = function(player)
        player:reduceMoney(-200)
        print(tostring(player) .. " received 200$ when crossing the start tile")
    end
}

local blumenstrasse1 = createBlumenstrasse("Blumenstrasse 13", 1)
local blumenstrasse2 = createBlumenstrasse("Blumenstrasse 13", 2)
local blumenstrasse3 = createBlumenstrasse("Blumenstrasse 13", 3)
local blumenstrasse4 = createBlumenstrasse("Blumenstrasse 13", 4)
local blumenstrasse5 = createBlumenstrasse("Blumenstrasse 13", 5)
local blumenstrasse6 = createBlumenstrasse("Blumenstrasse 13", 6)
local blumenstrasse7 = createBlumenstrasse("Blumenstrasse 13", 7)
local blumenstrasse8 = createBlumenstrasse("Blumenstrasse 13", 8)
local blumenstrasse9 = createBlumenstrasse("Blumenstrasse 13", 9)
local blumenstrasse10= createBlumenstrasse("Blumenstrasse 13", 10)
local blumenstrasse11= createBlumenstrasse("Blumenstrasse 13", 11)
local blumenstrasse12= createBlumenstrasse("Blumenstrasse 13", 12)
local blumenstrasse13= createBlumenstrasse("Blumenstrasse 13", 13)
local blumenstrasse14= createBlumenstrasse("Blumenstrasse 13", 14)
local blumenstrasse15= createBlumenstrasse("Blumenstrasse 13", 15)

local boardLayout = {}

for y = 1, 5 do
    boardLayout[y] = {}
    for x = 1, 5 do
        boardLayout[y][x] = createEmptySpace()
    end
end

boardLayout[5][5] = startTile
boardLayout[5][4] = blumenstrasse1
boardLayout[5][3] = blumenstrasse2
boardLayout[5][2] = blumenstrasse3
boardLayout[5][1] = blumenstrasse4
boardLayout[4][1] = blumenstrasse5
boardLayout[3][1] = blumenstrasse6
boardLayout[2][1] = blumenstrasse7
boardLayout[1][1] = blumenstrasse8
boardLayout[1][2] = blumenstrasse9
boardLayout[1][3] = blumenstrasse10
boardLayout[1][4] = blumenstrasse11
boardLayout[1][5] = blumenstrasse12
boardLayout[2][5] = blumenstrasse13
boardLayout[3][5] = blumenstrasse14
boardLayout[4][5] = blumenstrasse15

board:loadBoard(boardLayout)