from pathlib import Path
import re
from docx import Document
from docx.shared import Cm, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
MD = ROOT / "docs" / "paper" / "快递地址簿录入程序设计.md"
DOCX = ROOT / "deliverables" / "MS-09" / "快递地址簿录入程序设计.docx"
DOCX.parent.mkdir(parents=True, exist_ok=True)

doc = Document()
section = doc.sections[0]
section.page_width = Cm(21.0)
section.page_height = Cm(29.7)
section.top_margin = Cm(2.4)
section.bottom_margin = Cm(2.2)
section.left_margin = Cm(2.5)
section.right_margin = Cm(2.5)

styles = doc.styles
normal = styles["Normal"]
normal.font.name = "宋体"
normal.font.size = Pt(10.5)
normal.element.rPr.rFonts.set(qn("w:eastAsia"), "宋体")
normal.paragraph_format.line_spacing = 1.5
normal.paragraph_format.space_after = Pt(3)

for name, size in [("Heading 1", 18), ("Heading 2", 15), ("Heading 3", 13), ("Title", 26)]:
    st = styles[name]
    st.font.name = "黑体"
    st.element.rPr.rFonts.set(qn("w:eastAsia"), "黑体")
    st.font.size = Pt(size)
    st.font.color.rgb = RGBColor(0x0F, 0x17, 0x2A)
    st.paragraph_format.space_before = Pt(12)
    st.paragraph_format.space_after = Pt(6)

def set_run_font(run, name="宋体", size=10.5, bold=False, color=None):
    run.font.name = name
    run._element.get_or_add_rPr().rFonts.set(qn("w:eastAsia"), name)
    run.font.size = Pt(size)
    run.bold = bold
    if color:
        run.font.color.rgb = color

def add_field(paragraph, instruction, placeholder="右键更新域"):
    run = paragraph.add_run()
    fld_begin = OxmlElement("w:fldChar"); fld_begin.set(qn("w:fldCharType"), "begin")
    instr = OxmlElement("w:instrText"); instr.set(qn("xml:space"), "preserve"); instr.text = instruction
    sep = OxmlElement("w:fldChar"); sep.set(qn("w:fldCharType"), "separate")
    text = OxmlElement("w:t"); text.text = placeholder
    end = OxmlElement("w:fldChar"); end.set(qn("w:fldCharType"), "end")
    run._r.append(fld_begin); run._r.append(instr); run._r.append(sep); run._r.append(text); run._r.append(end)

def add_page_number(paragraph):
    run = paragraph.add_run()
    fld_begin = OxmlElement("w:fldChar"); fld_begin.set(qn("w:fldCharType"), "begin")
    instr = OxmlElement("w:instrText"); instr.set(qn("xml:space"), "preserve"); instr.text = "PAGE"
    sep = OxmlElement("w:fldChar"); sep.set(qn("w:fldCharType"), "separate")
    end = OxmlElement("w:fldChar"); end.set(qn("w:fldCharType"), "end")
    run._r.append(fld_begin); run._r.append(instr); run._r.append(sep); run._r.append(end)

def add_footer(section):
    footer = section.footer
    p = footer.paragraphs[0]
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    set_run_font(p.add_run("第 "), size=9)
    add_page_number(p)
    set_run_font(p.add_run(" 页"), size=9)

def add_code(lines):
    p = doc.add_paragraph()
    p.paragraph_format.left_indent = Cm(0.5)
    p.paragraph_format.space_before = Pt(4)
    p.paragraph_format.space_after = Pt(6)
    p.paragraph_format.line_spacing = 1.0
    shade = OxmlElement("w:shd"); shade.set(qn("w:fill"), "F1F5F9")
    p._p.get_or_add_pPr().append(shade)
    for i, line in enumerate(lines):
        r = p.add_run(line)
        set_run_font(r, "Consolas", 8.5)
        if i < len(lines) - 1:
            r.add_break()

def add_table(rows):
    if not rows:
        return
    table = doc.add_table(rows=len(rows), cols=len(rows[0]))
    table.style = "Table Grid"
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    for i, row in enumerate(rows):
        for j, cell in enumerate(row):
            c = table.cell(i, j)
            c.text = ""
            p = c.paragraphs[0]
            p.paragraph_format.line_spacing = 1.0
            r = p.add_run(re.sub(r"[`*]", "", cell))
            set_run_font(r, "宋体", 8.5, bold=(i == 0))
    doc.add_paragraph().paragraph_format.space_after = Pt(2)

def add_heading(level, text):
    p = doc.add_heading(level=level)
    r = p.add_run(text)
    set_run_font(r, "黑体", {1: 18, 2: 15, 3: 13, 4: 12}.get(level, 12), bold=True)
    if level == 1:
        p.paragraph_format.page_break_before = True
    return p

lines = MD.read_text(encoding="utf-8").splitlines()
i = 0
first_h1 = True
in_toc = False
skip_toc = False

while i < len(lines):
    raw = lines[i]
    line = raw.rstrip()

    if line == "---":
        i += 1
        continue

    if line.startswith("# "):
        text = line[2:].strip()
        if first_h1:
            p = doc.add_paragraph()
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            p.paragraph_format.space_before = Pt(100)
            r = p.add_run(text)
            set_run_font(r, "黑体", 26, bold=True)
            first_h1 = False
        else:
            add_heading(1, text)
        i += 1
        continue

    if line.startswith("## "):
        text = line[3:].strip()
        if text in ("摘要", "目录"):
            doc.add_page_break()
        add_heading(2, text)
        if text == "目录":
            in_toc = True
            skip_toc = True
            p = doc.add_paragraph()
            add_field(p, 'TOC \\o "1-3" \\h \\z \\u', "目录将在 Word 中更新")
        i += 1
        continue

    if line.startswith("### "):
        add_heading(3, line[4:].strip())
        i += 1
        continue

    if line.startswith("#### "):
        add_heading(4, line[5:].strip())
        i += 1
        continue

    if in_toc and skip_toc:
        if line.startswith("- ") or line.startswith("  - "):
            i += 1
            continue
        if line == "":
            i += 1
            continue
        skip_toc = False
        in_toc = False
        continue

    if line.startswith("```"):
        code_lines = []
        i += 1
        while i < len(lines) and not lines[i].startswith("```"):
            code_lines.append(lines[i])
            i += 1
        i += 1
        add_code(code_lines)
        continue

    if line.startswith("|"):
        table_rows = []
        while i < len(lines) and lines[i].startswith("|"):
            row = [c.strip() for c in lines[i].strip().strip("|").split("|")]
            if not all(re.fullmatch(r":?-{3,}:?", c) for c in row):
                table_rows.append(row)
            i += 1
        add_table(table_rows)
        continue

    if line.startswith("!["):
        m = re.match(r"!\[[^\]]*\]\(([^)]+)\)", line)
        if m:
            image_path = (MD.parent / m.group(1)).resolve()
            if image_path.exists():
                with Image.open(image_path) as im:
                    w, h = im.size
                max_w, max_h = 14.8, 21.2
                width_cm = max_w
                height_cm = width_cm * h / w
                if height_cm > max_h:
                    height_cm = max_h
                    width_cm = height_cm * w / h
                p = doc.add_paragraph()
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
                p.paragraph_format.space_after = Pt(2)
                p.add_run().add_picture(str(image_path), width=Cm(width_cm))
        i += 1
        continue

    if line.startswith("> "):
        p = doc.add_paragraph()
        p.paragraph_format.left_indent = Cm(0.6)
        p.paragraph_format.space_after = Pt(4)
        r = p.add_run(line[2:])
        set_run_font(r, "楷体", 9, color=RGBColor(0x64, 0x74, 0x8B))
        i += 1
        continue

    if line.startswith("- "):
        p = doc.add_paragraph(style="List Bullet")
        r = p.add_run(re.sub(r"[`*]", "", line[2:]))
        set_run_font(r, "宋体", 10.5)
        i += 1
        continue

    if line.strip() == "":
        i += 1
        continue

    if line.startswith("**图") or line.startswith("**表"):
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p.paragraph_format.space_before = Pt(2)
        p.paragraph_format.space_after = Pt(2)
        p.paragraph_format.keep_with_next = True
        r = p.add_run(re.sub(r"[`*]", "", line))
        set_run_font(r, "宋体", 9, bold=True)
        i += 1
        continue

    p = doc.add_paragraph()
    p.paragraph_format.first_line_indent = Cm(0.74)
    text = re.sub(r"[`*]", "", line)
    r = p.add_run(text)
    set_run_font(r, "宋体", 10.5)
    i += 1

for s in doc.sections:
    add_footer(s)

doc.save(DOCX)
print(DOCX)
