# Frontend Design Foundation

The global style layers are imported after Element Plus in this order:

1. `tokens.css`: primitive and semantic design tokens.
2. `element-plus.css`: Element Plus variables and shared component treatments.
3. `motion.css`: route transitions, reusable transition names, and reduced-motion behavior.
4. `main.css`: document defaults and cross-page layout utilities.

## Token Rules

- Use semantic tokens such as `--app-text`, `--app-border`, and `--app-surface` in components.
- Use palette tokens only when defining a new semantic state or chart series.
- Spacing follows a 4px grid through `--space-*` tokens.
- Product controls use 4px to 8px radii. Reserve `--radius-full` for avatars and status indicators.
- Keep surfaces flat. Use `--shadow-overlay` only for dialogs, drawers, and popovers.
- The six `--chart-color-*` values form the default categorical chart palette.

## Motion Rules

| Interaction | Token | Intended use |
| --- | --- | --- |
| Press | `--motion-duration-instant` | Button press feedback |
| Micro | `--motion-duration-fast` | Hover, focus, icon and color changes |
| Standard | `--motion-duration-base` | Routes, menus and local content changes |
| Emphasized | `--motion-duration-slow` | Drawers and reordered lists |
| Chart | `--motion-duration-chart` | First chart render only |

Use `app-route`, `motion-fade`, or `motion-slide` with Vue `Transition`. Avoid looping decorative motion. Chart updates should be shorter than initial chart entry.

`prefers-reduced-motion: reduce` collapses all duration and movement tokens to effectively static behavior. New motion must use the shared tokens so this remains reliable.

## Element Plus Overrides

Keep overrides in `element-plus.css` and target public component classes or documented CSS variables. Page-specific layouts remain in their Vue components. Do not add one-off global selectors for a single page.
