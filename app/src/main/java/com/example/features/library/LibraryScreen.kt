package com.example.features.library

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.constants.AppConstants.AppLanguage

data class ScriptureBook(
  val id: String,
  val title: String,
  val description: String,
  val chaptersCount: Int,
  val versesCount: Int,
  val contentPreview: String,
  val fullContent: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
  language: AppLanguage
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedBook by remember { mutableStateOf<ScriptureBook?>(null) }
  var isBookmarked by remember { mutableStateOf(false) }

  val books = listOf(
    ScriptureBook(
      "1",
      "Siddhant Vani",
      "Foundational teachings regarding the nature of soul, eternity, and supreme union.",
      12,
      144,
      "अक्षर अतीत पार है, ताके पार प्राननाथ। ताकी सेवा तुम करो, सदा राखो सिर हाथ॥",
      listOf(
        "अक्षर अतीत पार है, ताके पार प्राननाथ।\nताकी सेवा तुम करो, सदा राखो सिर हाथ॥\n\n(1) The Supreme Soul is transcendent beyond description. Worship Him always and feel His light within.",
        "परम प्रकाश रूप है, सकल जगत आधार।\nजाकी ज्योति जगमगे, सोई प्रान अधार॥\n\n(2) He is the embodiment of supreme illumination, the support of the entire universe. His light sparkles in every soul.",
        "प्रेम स्वरूप परमात्मा, सदा करत कल्यान।\nताकी सरनी जो रहे, ताको मिलत गियान॥\n\n(3) God is love itself, always bestowing supreme welfare. Whoever surrenders in His shelter attains eternal wisdom."
      )
    ),
    ScriptureBook(
      "2",
      "Tartam Sagar",
      "Holy revelations of Tartam Wisdom unlocking spiritual cosmic keys.",
      10,
      120,
      "तारतम ज्ञान सोई जानिये, जो काटे भव के बंध।",
      listOf(
        "तारतम ज्ञान सोई जानिये, जो काटे भव के बंध।\nपरम धाम को राह दिखावे, जोड़े आतम संबंध॥\n\n(1) Real spiritual wisdom is that which breaks the shackles of materialistic bondage and leads the soul back to Paramdham.",
        "सच्चिदानंद सरूप है, आदि अंत से पार।\nताको हृदय में धार के, तजो जगत बेकार॥\n\n(2) The Lord is Truth, Consciousness, and Bliss, beyond beginning and end. Hold Him in your heart and rise above temporary illusions."
      )
    ),
    ScriptureBook(
      "3",
      "Devotional Sunderkand",
      "Beautiful rendering of devotion, highlighting selfless surrender and strength.",
      5,
      60,
      "कवन सो काज कठिन जग माहीं। जो नहिं होइ तात तुम पाहीं॥",
      listOf(
        "कवन सो काज कठिन जग माहीं।\nजो नहिं होइ तात तुम पाहीं॥\n\n(1) What task is too difficult in this world that cannot be accomplished by pure devotion?",
        "राम काज लगि तव अवतारा।\nसुनतहिं भयउ पर्बताकारा॥\n\n(2) Hearing the call of divine service, his strength swelled like a golden mountain. Selfless action is divine."
      )
    )
  )

  val filteredBooks = books.filter {
    it.title.contains(searchQuery, ignoreCase = true) || 
    it.description.contains(searchQuery, ignoreCase = true)
  }

  AnimatedContent(
    targetState = selectedBook,
    transitionSpec = {
      if (targetState != null) {
        slideInHorizontally { width -> width } + fadeIn() togetherWith
        slideOutHorizontally { width -> -width } + fadeOut()
      } else {
        slideInHorizontally { width -> -width } + fadeIn() togetherWith
        slideOutHorizontally { width -> width } + fadeOut()
      }
    },
    label = "library_nav"
  ) { currentBook ->
    if (currentBook != null) {
      // ----------------------------------------------------
      // SCRIPTURE READER VIEW (Full screen reading experience)
      // ----------------------------------------------------
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 20.dp)
          .testTag("library_reading_view")
      ) {
        // Reader Header
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = { selectedBook = null }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
          }

          Text(
            text = currentBook.title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            fontFamily = FontFamily.Serif
          )

          IconButton(onClick = { isBookmarked = !isBookmarked }) {
            Icon(
              imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
              contentDescription = "Bookmark",
              tint = MaterialTheme.colorScheme.primary
            )
          }
        }

        Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))

        // Scripture Content Scrolling Panel
        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp)
        ) {
          currentBook.fullContent.forEachIndexed { index, verseText ->
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              shape = RoundedCornerShape(16.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            ) {
              Column(modifier = Modifier.padding(18.dp)) {
                Text(
                  text = "VERSE ${index + 1}",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.secondary,
                  letterSpacing = 1.sp,
                  modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                  text = verseText,
                  fontSize = 15.sp,
                  lineHeight = 24.sp,
                  fontWeight = FontWeight.Medium,
                  fontFamily = FontFamily.Serif,
                  color = MaterialTheme.colorScheme.onBackground
                )
              }
            }
          }
        }
      }
    } else {
      // ----------------------------------------------------
      // SCRIPTURE LIST VIEW
      // ----------------------------------------------------
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 20.dp, vertical = 12.dp)
          .testTag("library_list_view")
      ) {
        Text(
          text = "Holy Scriptures Library",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(bottom = 12.dp)
        )

        // Search scriptures
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search scriptures or Vani...", fontSize = 13.sp) },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
          shape = RoundedCornerShape(16.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
        )

        // Books list
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.weight(1f)
        ) {
          items(filteredBooks) { book ->
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { selectedBook = book },
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              shape = RoundedCornerShape(20.dp),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
              )
            ) {
              Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(40.dp)
                      .clip(RoundedCornerShape(8.dp))
                      .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.Book,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary
                    )
                  }

                  Spacer(modifier = Modifier.width(12.dp))

                  Column {
                    Text(
                      text = book.title,
                      fontSize = 15.sp,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary,
                      fontFamily = FontFamily.Serif
                    )
                    Text(
                      text = "${book.chaptersCount} chapters • ${book.versesCount} verses",
                      fontSize = 11.sp,
                      color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                  text = book.description,
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                  lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                    .border(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
                ) {
                  Text(
                    text = "“ ${book.contentPreview} ”",
                    fontSize = 11.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                    fontFamily = FontFamily.Serif
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
