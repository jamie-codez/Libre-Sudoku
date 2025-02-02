package com.kaajjo.libresudoku.ui.customsudoku

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kaajjo.libresudoku.R
import com.kaajjo.libresudoku.core.qqwing.GameType
import com.kaajjo.libresudoku.data.database.model.SavedGame
import com.kaajjo.libresudoku.ui.components.board.BoardPreview
import com.kaajjo.libresudoku.ui.components.EmptyScreen
import kotlin.math.sqrt
import kotlin.time.toKotlinDuration

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun CustomSudokuScreen(
    navigateBack: () -> Unit,
    navigateCreateSudoku: () -> Unit,
    navigatePlayGame: (Long) -> Unit,
    viewModel: CustomSudokuViewModel
) {
    val boards by viewModel.allBoards.collectAsState(initial = emptyList())

    var dialogDeleteConfirmation by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AnimatedContent(targetState = viewModel.inSelectionMode) {
                if(it) {
                    SelectionTopAppBar(
                        selectedCount = viewModel.selectedItems.count(),
                        onClickDeleteSelected = { dialogDeleteConfirmation = true },
                        onClickClose = { viewModel.inSelectionMode = false },
                        onClickSelectAll = { viewModel.addAllToSelection(boards)},
                        onClickInverseSelection = { viewModel.inverseSelection(boards) }
                    )
                } else {
                    DefaultTopAppBar(
                        onClickNavigationIcon = navigateBack,
                        scrollBehavior = scrollBehavior
                    )
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !viewModel.inSelectionMode,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                FloatingActionButton(
                    onClick = navigateCreateSudoku
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(top = 2.dp)
        ) {

            if(boards.isNotEmpty()) {
                val savedGames by viewModel.savedGames.collectAsState(initial = emptyList())
                LazyColumn {
                    itemsIndexed(
                        items = boards.reversed(),
                        key = { _, item -> item.uid }
                    ) { index, item ->
                        var savedGame by remember { mutableStateOf(savedGames.firstOrNull { it.uid == item.uid })}
                        LaunchedEffect(savedGames){
                            savedGame = savedGames.firstOrNull { it.uid == item.uid }
                        }
                        SudokuItem(
                            modifier = Modifier
                                .background(
                                    if (viewModel.inSelectionMode && viewModel.selectedItems.contains(
                                            item
                                        )
                                    ) {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                )
                                .animateItemPlacement(spring(0.6f, 300f)),
                            board = item.initialBoard,
                            uid = item.uid,
                            type = item.type,
                            savedGame = savedGame,
                            onClick = {
                                if(viewModel.inSelectionMode) {
                                    viewModel.addToSelection(item)
                                } else {
                                    navigatePlayGame(item.uid)
                                }
                            },
                            onLongClick = {
                                if (!viewModel.inSelectionMode) {
                                    viewModel.clearSelection()
                                    viewModel.inSelectionMode = true
                                    viewModel.addToSelection(item)
                                }
                            }
                        )
                        if(index + 1 < boards.size) {
                            Divider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(CircleShape)
                                    .padding(horizontal = 12.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            } else {
                EmptyScreen(
                    text = stringResource(R.string.custom_sudoku_no_added)
                )
            }
        }
        LaunchedEffect(viewModel.selectedItems.count()) {
            if(viewModel.selectedItems.isEmpty()) {
                viewModel.inSelectionMode = false
            }
        }
        if(dialogDeleteConfirmation) {
            DeleteConfirmationDialog(
                onClickConfirm = {
                    viewModel.deleteSelected()
                    dialogDeleteConfirmation = false
                },
                onDismiss = { dialogDeleteConfirmation = false }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SudokuItem(
    board: String,
    uid: Long,
    type: GameType,
    savedGame: SavedGame?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { },
    onLongClick: () -> Unit = { }
) {
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .then(modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .size(130.dp)
                    .align(Alignment.CenterVertically)
            ) {
                BoardPreview(
                    size = sqrt(board.length.toFloat()).toInt(),
                    boardString = board
                )
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
            ) {
                Column {
                    Text(stringResource(type.resName))
                    if(savedGame != null) {
                        Text(
                            stringResource(
                                R.string.saved_game_time,
                                savedGame.timer.toKotlinDuration().toComponents { minutes, seconds, _ ->
                                    String.format(" %02d:%02d", minutes, seconds)
                                }
                            )
                        )
                    } else {
                        Text(stringResource(R.string.game_not_started))
                    }
                    Text(stringResource(R.string.history_item_id, uid))
                }

            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultTopAppBar(
    onClickNavigationIcon: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(title = { Text(stringResource(R.string.custom_sudoku_title)) },
        navigationIcon = {
            IconButton(onClick = onClickNavigationIcon) {
                Icon(
                    painter = painterResource(R.drawable.ic_round_arrow_back_24),
                    contentDescription = null
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionTopAppBar(
    selectedCount: Int,
    onClickClose: () -> Unit,
    onClickDeleteSelected: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickInverseSelection: () -> Unit
) {
    TopAppBar(
        title = { Text(selectedCount.toString()) },
        navigationIcon = {
            IconButton(onClick = onClickClose) {
                Icon(
                    painter = painterResource(R.drawable.ic_round_close_24),
                    contentDescription = null
                )
            }
        },
        actions = {
            IconButton(onClick = onClickDeleteSelected) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = null
                )
            }
            IconButton(onClick = onClickSelectAll) {
                Icon(
                    painterResource(R.drawable.outline_select_all_24),
                    contentDescription = null
                )
            }
            IconButton(onClick = onClickInverseSelection) {
                Icon(
                    painterResource(R.drawable.outline_flip_to_back_24),
                    contentDescription = null
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)
        )
    )
}

@Composable
private fun DeleteConfirmationDialog(
    onClickConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        title = { Text(stringResource(R.string.custom_sudoku_delete_dialog_title)) },
        text = { Text(stringResource(R.string.custom_sudoku_delete_dialog_text))},
        confirmButton = {
            TextButton(onClick = onClickConfirm) {
                Text(stringResource(R.string.dialog_yes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_no))
            }
        },
        onDismissRequest = onDismiss
    )
}