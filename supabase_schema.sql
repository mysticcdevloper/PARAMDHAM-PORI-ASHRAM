-- =========================================================
-- PARAMDHAM PODHI ASHRAM
-- MODULE 3: PREMIUM REALTIME CHAT SYSTEM - SQL SCHEMA FOR SUPABASE
-- =========================================================

-- Enable uuid-ossp extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. GROUP ROLES enum / table
CREATE TYPE ashram_role AS ENUM (
  'Admin', 'Main Sanchalak', 'Adhyaksh', 'Upadhyaksh', 
  'Dharma Pracharak', 'Sachiv', 'Koshadhyaksh', 'Volunteer', 
  'Verified Member', 'Guest'
);

-- 2. CONVERSATIONS Table
CREATE TABLE conversations (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name VARCHAR(255) NOT NULL,
  description TEXT,
  is_group BOOLEAN DEFAULT false,
  avatar_url TEXT,
  invite_link TEXT,
  qr_code_text TEXT,
  created_by UUID REFERENCES auth.users(id) ON DELETE SET NULL,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. GROUP MEMBERS Table
CREATE TABLE group_members (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  conversation_id UUID REFERENCES conversations(id) ON DELETE CASCADE,
  member_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
  role ashram_role DEFAULT 'Verified Member',
  is_muted BOOLEAN DEFAULT false,
  joined_at TIMESTAMPTZ DEFAULT NOW(),
  approved_by UUID REFERENCES auth.users(id) ON DELETE SET NULL,
  is_approved BOOLEAN DEFAULT true,
  UNIQUE(conversation_id, member_id)
);

-- 4. MESSAGE TYPE Enum
CREATE TYPE message_type_enum AS ENUM (
  'TEXT', 'IMAGE', 'VIDEO', 'AUDIO', 'VOICE', 'DOCUMENT', 'STICKER', 'POLL'
);

-- 5. MESSAGES Table
CREATE TABLE messages (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  conversation_id UUID REFERENCES conversations(id) ON DELETE CASCADE,
  sender_id UUID REFERENCES auth.users(id) ON DELETE SET NULL,
  sender_name VARCHAR(255) NOT NULL,
  sender_role_icon VARCHAR(10) DEFAULT '🙏',
  content TEXT NOT NULL,
  type message_type_enum DEFAULT 'TEXT',
  status VARCHAR(20) DEFAULT 'READ', -- 'SENDING', 'DELIVERED', 'READ'
  replied_to_id UUID REFERENCES messages(id) ON DELETE SET NULL,
  is_forwarded BOOLEAN DEFAULT false,
  is_pinned BOOLEAN DEFAULT false,
  is_starred BOOLEAN DEFAULT false,
  scheduled_time TIMESTAMPTZ,
  timestamp TIMESTAMPTZ DEFAULT NOW()
);

-- 6. ATTACHMENTS Table
CREATE TABLE attachments (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  message_id UUID REFERENCES messages(id) ON DELETE CASCADE,
  name VARCHAR(255) NOT NULL,
  type VARCHAR(50) NOT NULL, -- 'image', 'video', 'audio', 'document', 'voice', 'sticker'
  url TEXT NOT NULL,
  size BIGINT NOT NULL,
  duration_sec INTEGER,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 7. POLL DATA Table
CREATE TABLE polls (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  message_id UUID REFERENCES messages(id) ON DELETE CASCADE,
  question TEXT NOT NULL,
  is_anonymous BOOLEAN DEFAULT false,
  is_multiple_choice BOOLEAN DEFAULT false,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 8. POLL OPTIONS Table
CREATE TABLE poll_options (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  poll_id UUID REFERENCES polls(id) ON DELETE CASCADE,
  text TEXT NOT NULL,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 9. POLL VOTES Table
CREATE TABLE poll_votes (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  option_id UUID REFERENCES poll_options(id) ON DELETE CASCADE,
  member_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  UNIQUE(option_id, member_id)
);

-- 10. MESSAGE REACTIONS Table
CREATE TABLE message_reactions (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  message_id UUID REFERENCES messages(id) ON DELETE CASCADE,
  sender_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
  sender_name VARCHAR(255) NOT NULL,
  emoji VARCHAR(50) NOT NULL,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  UNIQUE(message_id, sender_id)
);

-- 11. STATUS TYPE Enum
CREATE TYPE status_type_enum AS ENUM (
  'TEXT', 'PHOTO', 'VIDEO', 'AUDIO', 'PRAYER', 'VANI'
);

-- 12. MEMBERS STATUS Table
CREATE TABLE status (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  member_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
  member_name VARCHAR(255) NOT NULL,
  type status_type_enum DEFAULT 'TEXT',
  content TEXT,
  media_url TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 13. STATUS VIEWS Table
CREATE TABLE status_views (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  status_id UUID REFERENCES status(id) ON DELETE CASCADE,
  viewer_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
  viewer_name VARCHAR(255) NOT NULL,
  viewed_at TIMESTAMPTZ DEFAULT NOW(),
  UNIQUE(status_id, viewer_id)
);

-- 14. BROADCASTS Table
CREATE TABLE broadcasts (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  title VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  target_roles ashram_role[] DEFAULT '{}',
  sender_id UUID REFERENCES auth.users(id) ON DELETE SET NULL,
  sent_at TIMESTAMPTZ DEFAULT NOW()
);

-- 15. BOOKMARKS Table
CREATE TABLE bookmarks (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  member_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
  message_id UUID REFERENCES messages(id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  UNIQUE(member_id, message_id)
);

-- 16. DOWNLOADS Table
CREATE TABLE downloads (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  member_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
  file_name VARCHAR(255) NOT NULL,
  size BIGINT NOT NULL,
  progress REAL DEFAULT 0.0,
  status VARCHAR(50) DEFAULT 'QUEUED',
  url TEXT NOT NULL,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 17. TYPING STATUS Table
CREATE TABLE typing_status (
  conversation_id UUID REFERENCES conversations(id) ON DELETE CASCADE,
  member_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
  is_typing BOOLEAN DEFAULT false,
  updated_at TIMESTAMPTZ DEFAULT NOW(),
  PRIMARY KEY (conversation_id, member_id)
);

-- 18. PRESENCE Table (Online status, Last seen)
CREATE TABLE presence (
  member_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  online_status VARCHAR(50) DEFAULT 'OFFLINE',
  last_seen TIMESTAMPTZ DEFAULT NOW()
);


-- =========================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- =========================================================

ALTER TABLE conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE group_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE attachments ENABLE ROW LEVEL SECURITY;
ALTER TABLE polls ENABLE ROW LEVEL SECURITY;
ALTER TABLE poll_options ENABLE ROW LEVEL SECURITY;
ALTER TABLE poll_votes ENABLE ROW LEVEL SECURITY;
ALTER TABLE message_reactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE status ENABLE ROW LEVEL SECURITY;
ALTER TABLE status_views ENABLE ROW LEVEL SECURITY;
ALTER TABLE broadcasts ENABLE ROW LEVEL SECURITY;
ALTER TABLE bookmarks ENABLE ROW LEVEL SECURITY;
ALTER TABLE downloads ENABLE ROW LEVEL SECURITY;
ALTER TABLE typing_status ENABLE ROW LEVEL SECURITY;
ALTER TABLE presence ENABLE ROW LEVEL SECURITY;

-- 1. Conversation Access Rule: Members can view conversations they are part of
CREATE POLICY select_conversations ON conversations
  FOR SELECT USING (
    id IN (SELECT conversation_id FROM group_members WHERE member_id = auth.uid())
    OR is_group = false
  );

-- 2. Message Read Access Rule
CREATE POLICY select_messages ON messages
  FOR SELECT USING (
    conversation_id IN (SELECT conversation_id FROM group_members WHERE member_id = auth.uid() AND is_approved = true)
  );

-- 3. Message Insert Access Rule
CREATE POLICY insert_messages ON messages
  FOR INSERT WITH CHECK (
    conversation_id IN (SELECT conversation_id FROM group_members WHERE member_id = auth.uid() AND is_approved = true AND is_muted = false)
  );

-- 4. Status Access Rule: Any authenticated ashram member can view statuses
CREATE POLICY select_status ON status
  FOR SELECT USING (true);


-- =========================================================
-- STORAGE BUCKETS CONFIGURATION INSTRUCTIONS
-- =========================================================
-- Create the following storage buckets in the Supabase Dashboard:
-- 1. 'chat-images'    - Public access for shared photos
-- 2. 'chat-videos'    - Public access for shared clips
-- 3. 'chat-documents' - Restrictive access for scriptures and PDFs
-- 4. 'chat-audio'     - Public access for satsangs and chants
-- 5. 'voice-notes'    - Authenticated access for voice logs
-- 6. 'stickers'       - Read-only public access for divine sticker sets
-- 7. 'status-media'   - Expiring access for 24h status photos/videos
