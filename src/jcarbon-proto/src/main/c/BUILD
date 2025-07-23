genrule(
  name = "copy_link_jni_md_header",
  srcs = ["@bazel_tools//tools/jdk:jni_md_header-linux"],
  outs = ["jni_md.h"],
  cmd = "cp -f $< $@",
)

genrule(
  name = "copy_link_jni_header",
  srcs = ["@bazel_tools//tools/jdk:jni_header"],
  outs = ["jni.h"],
  cmd = "cp -f $< $@",
)


cc_library(
  name = "copy_jni_hdr_lib",
  visibility = ["//visibility:public"],
  hdrs = [
      ":copy_link_jni_header",
      ":copy_link_jni_md_header",
  ],
  includes = ["."],
)