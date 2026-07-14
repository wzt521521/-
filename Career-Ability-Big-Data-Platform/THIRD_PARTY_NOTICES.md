# Third-Party Notices

## PDF rendering

`com.openhtmltopdf:openhtmltopdf-pdfbox:1.0.10` is used to render the PDF-only
report feature. It is distributed under LGPL-2.1. The project keeps the library
as an unmodified Maven dependency. Redistributing a binary must retain its
copyright and license notices and provide the corresponding LGPL source offer
required by that license. Upstream source: https://github.com/danfickle/openhtmltopdf

The transitive Apache PDFBox components are licensed under Apache-2.0. Their
notices are retained by the dependency artifacts.

`com.itextpdf:html2pdf:5.0.5` was removed because its AGPL/commercial licensing
does not fit this MIT-licensed repository's release model.

## Chinese font

The runtime image installs Noto Sans CJK from Alpine's `font-noto-cjk` package.
Noto fonts are licensed under the SIL Open Font License 1.1. The application
embeds the configured Noto Sans CJK font in generated PDF documents so Chinese
titles, tables, and punctuation do not depend on a viewer's local font set.
