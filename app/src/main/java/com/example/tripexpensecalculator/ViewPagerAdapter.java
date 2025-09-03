public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position) {
            case 0: return new FriendsFragment();
            case 1: return new ExpenseFragment();
            case 2: return new SummaryFragment();
            case 3: return new ReportFragment();
            default: return new FriendsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
