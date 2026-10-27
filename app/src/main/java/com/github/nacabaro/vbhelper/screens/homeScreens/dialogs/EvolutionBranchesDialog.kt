package com.github.nacabaro.vbhelper.screens.homeScreens.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.nacabaro.vbhelper.R
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.dtos.CharacterDtos
import com.github.nacabaro.vbhelper.utils.BitmapData
import com.github.nacabaro.vbhelper.utils.getBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val MAX_NODES = 200

private data class BranchNode(
    val depth: Int,
    val form: CharacterDtos.CardCharacterOption
)

/** Every form reachable from [fromCharaId], depth first so each branch reads together. */
@Composable
fun EvolutionBranchesDialog(
    fromCharaId: Long,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { (context.applicationContext as VBHelper).container.db }

    val nodes by produceState(initialValue = emptyList<BranchNode>(), fromCharaId) {
        value = withContext(Dispatchers.IO) {
            val result = mutableListOf<BranchNode>()
            suspend fun walk(id: Long, depth: Int, path: Set<Long>) {
                for (next in database.characterDao().getNextForms(id)) {
                    if (result.size >= MAX_NODES || next.id in path) continue
                    result.add(BranchNode(depth, next))
                    walk(next.id, depth + 1, path + next.id)
                }
            }
            walk(fromCharaId, 0, setOf(fromCharaId))
            result
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.home_evolve_view),
                    style = MaterialTheme.typography.titleLarge
                )

                if (nodes.isEmpty()) {
                    Text(
                        text = stringResource(R.string.home_evolve_none),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .heightIn(max = 460.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    for (node in nodes) {
                        val sprite = remember(node.form.id) {
                            BitmapData(node.form.spriteIdle, node.form.spriteWidth, node.form.spriteHeight)
                                .getBitmap().asImageBitmap()
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = (node.depth * 20).dp, top = 4.dp, bottom = 4.dp)
                        ) {
                            Image(
                                bitmap = sprite,
                                contentDescription = null,
                                filterQuality = FilterQuality.None,
                                modifier = Modifier.size(48.dp)
                            )
                            Column {
                                if (node.form.nameWidth > 0) {
                                    val name = remember(node.form.id) {
                                        BitmapData(node.form.nameSprite, node.form.nameWidth, node.form.nameHeight)
                                            .getBitmap().asImageBitmap()
                                    }
                                    Image(
                                        bitmap = name,
                                        contentDescription = null,
                                        filterQuality = FilterQuality.None,
                                        modifier = Modifier.height(node.form.nameHeight.dp * 2)
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.settings_create_character_stage, node.form.stage, node.form.charaIndex),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.item_dialog_cancel))
                    }
                }
            }
        }
    }
}
