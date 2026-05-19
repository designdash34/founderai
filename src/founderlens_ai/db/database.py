import os
from supabase import create_client, Client
from dotenv import load_dotenv

load_dotenv()

SUPABASE_URL = os.getenv("SUPABASE_URL")
SUPABASE_KEY = os.getenv("SUPABASE_KEY")

class SupabaseManager:
    _instance = None
    client: Client = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super(SupabaseManager, cls).__new__(cls)
            if SUPABASE_URL and SUPABASE_KEY:
                try:
                    cls.client = create_client(SUPABASE_URL, SUPABASE_KEY)
                except Exception as e:
                    print(f"Failed to initialize Supabase client: {e}")
        return cls._instance

    def is_active(self) -> bool:
        return self.client is not None

db_manager = SupabaseManager()
