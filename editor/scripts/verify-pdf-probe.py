#!/usr/bin/env python3
"""E-04 PDF gate: independent pypdf analysis of Android-produced PDF bytes.

This checks real text extraction, A5 MediaBox and basic visual text bounds.
A bitmap-only PDF fails because extract_text has no searchable glyphs.
"""
import math
import sys
from pypdf import PdfReader

path = sys.argv[1]
reader = PdfReader(path)
pages = reader.pages
assert 2 <= len(pages) <= 100, f"Expected multi-page A5 PDF: {len(pages)}"
expected_w = 148 / 25.4 * 72
expected_h = 210 / 25.4 * 72
all_text = []
for index, page in enumerate(pages):
    box = page.mediabox
    width = float(box.width)
    height = float(box.height)
    assert abs(width - expected_w) < 1.0, f"Page {index + 1} width {width}"
    assert abs(height - expected_h) < 1.0, f"Page {index + 1} height {height}"
    extracted = page.extract_text()
    all_text.append(extracted)
    assert extracted.strip(), f"Page {index + 1} contains no selectable text"
    print(f"A5 page {index+1}: {width:.3f} x {height:.3f} pt, "
          f"extracted characters={len(extracted)}")
content = "".join(all_text)
for token in ("Absatz", "Äpfel", "Öl", "Grüße", "Straße"):
    assert token in content, f"Missing searchable Unicode token: {token!r}"
# Ensure the PDF is made of text objects, not page screenshots with hidden OCR.
for index, page in enumerate(pages):
    fonts = page.get("/Resources", {}).get("/Font", {})
    assert fonts, f"Page {index + 1}: no PDF fonts"
    images = page.images
    assert len(images) == 0, f"Page {index + 1}: rasterized content is not a text PDF"
print("E-04 independent PDF parser checks passed.")
