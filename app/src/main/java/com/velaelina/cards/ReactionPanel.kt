package com.velaelina.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Reusable reaction renderer: the supplied illustration stays a single layer. */
@Composable
internal fun ReactionPanel(kind:GameReaction, bitmap:ImageBitmap?, modifier:Modifier=Modifier) {
    Column(modifier.testTag("reaction_"+kind.name.lowercase(java.util.Locale.ROOT)),
        horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text(stringResource(when(kind){
            GameReaction.ERROR -> R.string.ups
            GameReaction.STREAK -> R.string.reaction_streak
            GameReaction.COMPLETE -> R.string.parts_completed
        }),color=VelaPalette.Gold,fontSize=34.sp,fontWeight=FontWeight.ExtraBold,textAlign=TextAlign.Center)
        if(bitmap!=null) Image(bitmap,stringResource(when(kind){
            GameReaction.ERROR -> R.string.ups_art
            GameReaction.STREAK -> R.string.reaction_streak_art
            GameReaction.COMPLETE -> R.string.reaction_finish_art
        }),Modifier.weight(1f).fillMaxWidth(),contentScale=ContentScale.Fit)
    }
}
