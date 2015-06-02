import json, sys

def getErrorMap(filename):
  em = json.load(open(filename))
  errs = {}
  for id in em:
    for item in em[id]:
      errs.setdefault(item["error_type"], set()).add(id)
  return errs, len(em)

def getarrow(new, old):
  arrow = "="
  if new > old:
    arrow = "{\\color{red}$\uparrow$}(" + str(new - old) + ")"
  elif new < old:
    arrow = "{\\color{green}$\downarrow$}(" + str(old - new) + ")"
  return arrow

def percent(count, total):
  return "%.3f" % (100*float(count)/float(total))

def getTable(em, cm, count):
  counted = sorted(
      [(
          pack, 
          getarrow(len(em[pack]), len(cm.setdefault(pack,[]))) + " " +
          str(len(em[pack])), 
          percent(len(em[pack]), count)) 
        for pack in em], 
      key = lambda x: int(x[1].split()[-1]), 
      reverse = True) if cm != {} else sorted(
      [(
          pack, 
          str(len(em[pack])), 
          percent(len(em[pack]), count)) 
        for pack in em], 
      key = lambda x: int(x[1]), 
      reverse = True)
  return preamble + "\n".join([" & ".join(item) + "\\\\" for item in counted]) + tail

to_compare = len(sys.args) > 3

ERRORMAP = sys.argv[1]
COMPAREMAP = sys.argv[2] if to_compare else ""
OUTPUT = sys.argv[3] if to_compare else sys.argv[2]

err2, count = getErrorMap(COMPAREMAP) if to_compare else ({}, 0)
err1, count = getErrorMap(ERRORMAP)

preamble = ("\\begin{table}[!t]\n" + 
      "\\renewcommand{\\arraystretch}{1.3}\n" + 
      "\\caption{Build Errors - \\com{NAME}}\n" +
      "\\centering\n" +
      "\\begin{tabular}{p{5cm}\tr\tr}\n" +
      "\\hline\n" +
      "\\textbf{Error Type} & \\textbf{\\# projects} & \\textbf{\\% projects}\\\\\n" +
      "\\hline\n")
tail = "\n\\hline\n\\end{tabular}\n\\label{NAME}\n\\end{table}\n"
table = getTable(err1, err2, count)
open(OUTPUT, "w").write(table)
