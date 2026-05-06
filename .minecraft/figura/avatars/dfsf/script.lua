-- Mirror @MagicLab --
local BoneInit = require("extrabone_lib")

local function compareVersion(v1, v2)
    local i1, i2 = 1, 1

    while true do
        local n1 = tonumber(v1:match("(%d+)", i1))
        local n2 = tonumber(v2:match("(%d+)", i2))

        if not n1 and not n2 then
            return 0
        end

        n1 = n1 or 0
        n2 = n2 or 0

        if n1 > n2 then
            return 1
        elseif n1 < n2 then
            return -1
        end

        i1 = v1:find("%.", i1, true)
        i2 = v2:find("%.", i2, true)

        if i1 then i1 = i1 + 1 end
        if i2 then i2 = i2 + 1 end

        if not i1 and not i2 then
            return 0
        end
    end
end

local function isVersionAtLeast(current, required)
    return compareVersion(current, required) >= 0
end

local version = client:getVersion()
local isNew = isVersionAtLeast(version, "1.21.1")

local chestType = isNew and "torso" or "body"

BoneInit({
    { models.model_blend.root.Neck,                             "body" },
    { models.model_blend.root.Body.chest,                       chestType },
    { models.model_blend.root.LeftShoulder,                     "body" },
    { models.model_blend.root.RightShoulder,                    "body" },
    { models.model_blend.root.LeftShoulder.LeftArm.LArmLower,   "leftArm" },
    { models.model_blend.root.RightShoulder.RightArm.RArmLower, "rightArm" },
    { models.model_blend.root.LeftLeg.LLegLower,                "leftLeg" },
    { models.model_blend.root.RightLeg.RLegLower,               "rightLeg" },
})

--hide vanilla model
vanilla_model.PLAYER:setVisible(false)

--hide vanilla armor model
vanilla_model.ARMOR:setVisible(false)
--re-enable the helmet item
vanilla_model.HELMET_ITEM:setVisible(true)

--hide vanilla cape model
vanilla_model.CAPE:setVisible(false)

--hide vanilla elytra model
vanilla_model.ELYTRA:setVisible(false)

--entity init event, used for when the avatar entity is loaded for the first time
function events.entity_init()
  --player functions goes here
end

--tick event, called 20 times per second
function events.tick()
  --code goes here
end

--render event, called every time your avatar is rendered
--it have two arguments, "delta" and "context"
--"delta" is the percentage between the last and the next tick (as a decimal value, 0.0 to 1.0)
--"context" is a string that tells from where this render event was called (the paperdoll, gui, player render, first person)
function events.render(delta, context)
  --code goes here
end
