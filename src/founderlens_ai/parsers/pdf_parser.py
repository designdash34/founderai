import fitz  # PyMuPDF
import re
import datetime

class PDFParser:
    def parse(self, inputs: dict) -> dict:
        file_path = inputs.get("pdf_path")
        document_text = inputs.get("document_text")
        
        if file_path:
            return self.parse_file(file_path)
        elif document_text:
            return self.parse_text(document_text)
        else:
            return self.get_default_document()

    def parse_file(self, file_path: str) -> dict:
        try:
            doc = fitz.open(file_path)
            full_text = ""
            for page in doc:
                full_text += page.get_text()
            
            return {
                "type": "document",
                "source": "user_uploaded",
                "title": self.extract_title(full_text),
                "date": self.extract_date(full_text),
                "full_text": full_text,
                "page_count": len(doc),
                "key_metrics": self.extract_numbers(full_text),
                "problems_stated": self.extract_problems(full_text),
                "quality_score": self.calculate_quality(full_text),
                "warnings": []
            }
        except Exception as e:
            return self.get_default_document()

    def parse_text(self, text: str) -> dict:
        return {
            "type": "document",
            "source": "user_provided_text",
            "title": self.extract_title(text),
            "date": self.extract_date(text),
            "full_text": text,
            "page_count": 1,
            "key_metrics": self.extract_numbers(text),
            "problems_stated": self.extract_problems(text),
            "quality_score": self.calculate_quality(text),
            "warnings": []
        }

    def extract_title(self, text: str) -> str:
        lines = text.split('\n')
        for line in lines[:5]:
            if len(line.strip()) > 5:
                return line.strip()
        return "Untitled Business Report"

    def extract_date(self, text: str) -> str:
        # Simple regex for YYYY-MM-DD or DD/MM/YYYY
        match = re.search(r'\d{4}-\d{2}-\d{2}', text)
        if match:
            return match.group()
        return datetime.datetime.now().strftime("%Y-%m-%d")

    def extract_numbers(self, text: str) -> dict:
        # Simple extraction of common metrics
        metrics = {}
        churn = re.search(r'churn.*?(\d+)%', text, re.IGNORECASE)
        if churn:
            metrics["churn_rate"] = float(churn.group(1))
        
        mrr = re.search(r'mrr.*?\$?(\d+[,.]?\d*)', text, re.IGNORECASE)
        if mrr:
            metrics["mrr"] = float(mrr.group(1).replace(',', ''))
            
        return metrics

    def extract_problems(self, text: str) -> list:
        problems = []
        keywords = ["risk", "problem", "issue", "decline", "friction", "pressure"]
        lines = text.split('.')
        for line in lines:
            if any(kw in line.lower() for kw in keywords):
                problems.append(line.strip())
        return problems[:5]

    def calculate_quality(self, text: str) -> int:
        if not text: return 0
        score = min(100, len(text) / 10)
        if "mrr" in text.lower(): score += 10
        if "churn" in text.lower(): score += 10
        return int(min(100, score))

    def get_default_document(self) -> dict:
        return {
            "type": "document",
            "source": "auto_filled",
            "title": "FounderLens AI — SaaS Business Report Q1 2026",
            "date": "2026-01-15",
            "full_text": "SaaS startup showing declining retention metrics. Monthly churn increased from 18% to 28% over Q1 2026. MRR dropped from $18,000 to $12,000. Customer complaints increased by 34%. Onboarding completion rate fell to 41%. Competitor pricing pressure identified as key risk.",
            "key_metrics": {
                "churn_rate": 28.0,
                "mrr": 12000,
                "retention_rate": 41.0,
                "complaint_increase": 34.0,
                "onboarding_completion": 41.0
            },
            "problems_stated": [
                "Rising churn rate",
                "MRR decline",
                "Poor onboarding completion",
                "Competitor pricing pressure"
            ],
            "quality_score": 85,
            "warnings": []
        }

def parse_pdf(file_path: str) -> dict:
    """Helper module-level function to parse a PDF file directly."""
    return PDFParser().parse({"pdf_path": file_path})
