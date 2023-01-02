package com.avasoft.androiddemo.Helpers.UIComponents

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Loader(isVisible:Boolean){
    AnimatedVisibility(modifier = Modifier.fillMaxSize()
        ,visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Surface(color = Color.Transparent) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.5f))
            ) {
                Card(modifier = Modifier
                    .height(120.dp)
                    .width(140.dp)
                    .align(Alignment.Center)
                    .animateEnterExit(
                        enter = slideInHorizontally(),
                        exit = slideOutHorizontally() { it }),
                    elevation = 10.dp,
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Column(modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(top = 10.dp))
                        Text(
                            text = "Loading",
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(vertical = 10.dp, horizontal = 10.dp)
                        )
                    }
                }
            }
        }
    }
}