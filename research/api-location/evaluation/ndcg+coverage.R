# Run commands after runnning loadData.R
# Run each sub-sections (tests) separately, ie donot source this entire document in R

# ndcg
subset(eval, (METRIC == "ndcg" & QUERY == "all"), select=c("QUERY","VALUE","SCHEME"))

# for coverage see:
cR10
# compute coverage for B1
length(cR10noNA$B1[cR10noNA$B1>0])/20*100
# and so on .. for B2, S1, S2, S3