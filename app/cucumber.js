module.exports = {
  default: [
    "--require-module ts-node/register",
    "--require src/steps/**/*.ts",
    "--require src/support/**/*.ts",
    "src/features/**/*.feature"
  ].join(" "),
};
