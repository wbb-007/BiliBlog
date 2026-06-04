---
name: Pink Pulse
colors:
  surface: '#faf9fd'
  surface-dim: '#dbd9dd'
  surface-bright: '#faf9fd'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f4f3f7'
  surface-container: '#efedf1'
  surface-container-high: '#e9e7ec'
  surface-container-highest: '#e3e2e6'
  on-surface: '#1a1b1e'
  on-surface-variant: '#564145'
  inverse-surface: '#2f3033'
  inverse-on-surface: '#f2f0f4'
  outline: '#897175'
  outline-variant: '#ddbfc4'
  surface-tint: '#a83159'
  primary: '#a83159'
  on-primary: '#ffffff'
  primary-container: '#fb7299'
  on-primary-container: '#710032'
  inverse-primary: '#ffb1c2'
  secondary: '#00658b'
  on-secondary: '#ffffff'
  secondary-container: '#33bffe'
  on-secondary-container: '#004b68'
  tertiary: '#006e25'
  on-tertiary: '#ffffff'
  tertiary-container: '#46b557'
  on-tertiary-container: '#004112'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#ffd9e0'
  primary-fixed-dim: '#ffb1c2'
  on-primary-fixed: '#3f0019'
  on-primary-fixed-variant: '#881741'
  secondary-fixed: '#c5e7ff'
  secondary-fixed-dim: '#7ed0ff'
  on-secondary-fixed: '#001e2d'
  on-secondary-fixed-variant: '#004c6a'
  tertiary-fixed: '#8bfb93'
  tertiary-fixed-dim: '#6fdd7a'
  on-tertiary-fixed: '#002106'
  on-tertiary-fixed-variant: '#00531a'
  background: '#faf9fd'
  on-background: '#1a1b1e'
  surface-variant: '#e3e2e6'
typography:
  display-lg:
    fontFamily: beVietnamPro
    fontSize: 48px
    fontWeight: '800'
    lineHeight: 56px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: beVietnamPro
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
  headline-lg-mobile:
    fontFamily: beVietnamPro
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
  headline-md:
    fontFamily: beVietnamPro
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: notoSans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: notoSans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 22px
  label-md:
    fontFamily: beVietnamPro
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 4px
  xs: 8px
  sm: 12px
  md: 16px
  lg: 24px
  xl: 32px
  gutter: 20px
  margin-mobile: 12px
  margin-desktop: 60px
---

## Brand & Style
The design system draws inspiration from high-energy ACG (Anime, Comics, and Games) communities, specifically channeling the vibrant and youthful spirit of contemporary video-sharing platforms. The brand personality is energetic, playful, and deeply community-focused, prioritizing content discovery and creator-audience interaction.

The visual style is **Corporate Modern with a Playful Edge**, characterized by clean white surfaces, vibrant accent colors, and a highly approachable "bubbly" geometry. It avoids the austerity of traditional SaaS by using softer corners and a saturated palette that evokes a sense of fun and digital optimism. The goal is to make the user feel like they are entering a bustling, creative hub where content is the protagonist.

## Colors
The palette is dominated by the signature "Bili-Pink," used for primary actions, branding elements, and active states. A "Sky-Blue" secondary color provides a refreshing contrast, typically used for informational tags, links, and secondary interactive elements.

- **Primary (#FB7299):** Used for CTA buttons, primary icons, and progress bars.
- **Secondary (#00AEEC):** Used for verification badges, secondary tags, and link highlights.
- **Neutral (#18191C):** A deep charcoal for high-contrast typography, ensuring maximum readability.
- **Surface (#FFFFFF):** Pure white for cards and content containers.
- **Background (#F1F2F3):** A soft, cool grey that allows white cards to pop with subtle depth.

## Typography
The typography system balances the friendly, contemporary feel of **Be Vietnam Pro** for headings and labels with the universal clarity of **Noto Sans** for body copy. 

Headlines utilize heavy weights and tight letter spacing to create a bold, "editorial" feel common in digital media. Body text is optimized for long-form reading and rapid scanning of video titles or blog snippets. Labels are kept concise and bold to facilitate quick navigation within a dense card-based UI.

## Layout & Spacing
The layout follows a **Fluid Grid** system designed around a modular card-based architecture. 

- **Desktop (1200px+):** A 12-column grid with 20px gutters. Content is organized into cards that typically span 3 columns (4 cards per row) for feeds, or 8 columns for main article content.
- **Tablet (768px - 1199px):** An 8-column grid. Cards reflow to 2 columns (4 per row) or 1 column depending on the content density.
- **Mobile (<767px):** A 4-column grid with 12px margins. Cards span the full width or 2 columns (2 per row) to maximize thumb-friendliness.

Spacing is based on a 4px baseline, but defaults to generous 24px and 32px gaps between major sections to prevent the UI from feeling cluttered despite the high content density.

## Elevation & Depth
This design system utilizes **Tonal Layers** combined with **Ambient Shadows** to create a sense of organized hierarchy.

1.  **Level 0 (Background):** The `#F1F2F3` canvas.
2.  **Level 1 (Cards/Surface):** Pure white `#FFFFFF` cards with a soft, expansive shadow (`0px 4px 20px rgba(0,0,0,0.05)`).
3.  **Level 2 (Hover States/Popovers):** Increased shadow depth and a slight upward translate (-2px) to provide tactile feedback during interaction.

The depth is intentional but subtle, ensuring that the "white-on-grey" contrast remains the primary driver of visual separation.

## Shapes
The shape language is the defining characteristic of this system. It uses **Extremely Rounded** corners to communicate a soft, approachable, and "kawaii" aesthetic.

- **Main Cards:** 20px - 24px corner radius.
- **Buttons & Inputs:** 12px - 16px corner radius.
- **Tags & Badges:** Fully pill-shaped (999px).
- **Avatars:** Circular (50%) to distinguish people from content thumbnails.

This high degree of roundedness reduces visual tension and makes the interface feel more like a physical toy or a polished consumer app than a rigid blog.

## Components

- **Cards:** The core unit. Cards must have a 20px radius, white background, and a subtle shadow. Thumbnails within cards should have a 12px radius. Content titles should use `headline-md` with a maximum of 2 lines.
- **Buttons:**
    - *Primary:* Filled `#FB7299` with white text. High-contrast, bold weight.
    - *Secondary:* Ghost style with `#00AEEC` borders and text.
- **Chips/Tags:** Used for categories and metadata. These are pill-shaped with light tinted backgrounds (e.g., 10% opacity of the accent color) and bold centered text.
- **Input Fields:** Large, 12px rounded corners, with a `#F1F2F3` background that shifts to white with a `#FB7299` border on focus.
- **Interaction Feedback:** Elements should use a slight scale-down effect (0.98) on click/active states to mimic a "squishy" physical button.
- **Progress Indicators:** Use the primary pink for all loading bars and video progress sliders to maintain brand consistency.