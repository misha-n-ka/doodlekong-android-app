package com.plcourse.mkirilkin.data.models.messages

import ru.mkirilkin.doodlekong.data.remote.websocket.models.messages.BaseModel
import ru.mkirilkin.doodlekong.util.Constants.TYPE_PING


class Ping : BaseModel(TYPE_PING)
